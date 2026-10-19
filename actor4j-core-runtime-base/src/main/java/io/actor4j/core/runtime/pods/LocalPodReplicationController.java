/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.runtime.pods;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import io.actor4j.core.function.Procedure;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.InternalPodActorCell;
import io.actor4j.core.runtime.factory.DefaultFactoryRegistry;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;

import static io.actor4j.core.actors.Actor.*;
import static io.actor4j.core.logging.ActorLogger.*;

public class LocalPodReplicationController implements PodReplicationController {
	protected final InternalActorSystem system;
	
	protected final DefaultFactoryRegistry<String> registry;
	protected final Map<String, PodReplicationTuple> podReplicationMap;
	
	protected final Function<String, Boolean> hasPrimaryReplica;
	
	protected final ReentrantLock lock = new ReentrantLock(); 
	
	public LocalPodReplicationController(InternalActorSystem system) {
		super();
		this.system = system;
		
		registry = new DefaultFactoryRegistry<>();
		podReplicationMap = new ConcurrentHashMap<>();
		
		hasPrimaryReplica = (domain) -> system.primaryPodDeployed(domain);
	}
	
	@Override
	public Map<String, PodReplicationTuple> getPodReplicationMap() {
		return podReplicationMap;
	}

	
	@Override
	public void reset() {
		registry.clear();
		podReplicationMap.clear();
	}

	@Override
	public void deployPods(File jarFile, PodConfiguration podConfiguration) {
		lock.lock();
		try {
			PodSystemConfiguration podSystemConfiguration = scalingAlgorithm(podConfiguration);
			podReplicationMap.put(podConfiguration.domain(), new PodReplicationTuple(podConfiguration, podSystemConfiguration, jarFile.getAbsolutePath()));
			if (podSystemConfiguration!=null)
				deployPods(jarFile, podConfiguration, podSystemConfiguration, system);
		}
		finally {
			lock.unlock();
		}
	}
	
	protected void deployPods(File jarFile, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, InternalActorSystem system) {
		systemLogger().log(ERROR, String.format("[REPLICATION] Domain '%s'cannot be deployed (not implemented)", podConfiguration.domain()));
	}
	
	@Override
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration) {
		lock.lock();
		try {
			PodSystemConfiguration podSystemConfiguration = scalingAlgorithm(podConfiguration);
			podReplicationMap.put(podConfiguration.domain(), new PodReplicationTuple(podConfiguration, podSystemConfiguration));
			if (podSystemConfiguration!=null) {
				registry.register(podConfiguration.domain(), factory);
				PodDeployment.deployPods(factory, podConfiguration, podSystemConfiguration, system, hasPrimaryReplica);
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void undeployPod(String domain, String shardId, int instances) {
		lock.lock();
		try {
			PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
			
			if (podReplicationTuple!=null) {
				if (podReplicationTuple.podConfiguration().shardCount()==1) {
					int allowed = Math.min(instances, podReplicationTuple.podSystemConfiguration().currentReplicaCount()-podReplicationTuple.podConfiguration().minReplica());
					if (allowed<instances)
						systemLogger().log(WARN, String.format("[REPLICATION] Domain '%s': limited to minReplica (%d)", domain, podReplicationTuple.podConfiguration().minReplica()));
					else
						systemLogger().log(INFO, String.format("[REPLICATION] Pod (%s) undeploying", domain));
					
					Queue<ActorId> queue = system.getPodDomains().get(domain);
					int count=0;
					if (queue!=null) {
						Iterator<ActorId> iterator = queue.iterator();
						for (; iterator.hasNext() && count<allowed;) {
							ActorId id = iterator.next();
							InternalPodActorCell cell = (InternalPodActorCell)id;
							if (!cell.getContext().primaryReplica()) { // does not remove primary replica
								systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
								system.send(ActorMessage.create(null, STOP, system.SYSTEM_ID(), id));
								iterator.remove();
								count++;
							}
						}
					}
					if (count>0) {
						PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
								null, null, null, 1, podReplicationTuple.podSystemConfiguration().currentReplicaCount()-count);
						podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration, podReplicationTuple.jarFileName()));
					}
				}
				else if (shardId==null || !podReplicationTuple.podSystemConfiguration().secondaryShardIds().contains(shardId))
					systemLogger().log(WARN, String.format("[REPLICATION] Domain '%s': unknown shard (%s)", domain, shardId));
				else {
					int index = Integer.parseInt(shardId);
					int allowed = Math.min(instances, podReplicationTuple.podSystemConfiguration().secondaryShardCounts().get(index)+1-podReplicationTuple.podConfiguration().minReplica()); // +1 primary
					if (allowed<instances)
						systemLogger().log(WARN, String.format("[REPLICATION] Shard (%s, %s): limited to minReplica (%d)", domain, shardId, podReplicationTuple.podConfiguration().minReplica()));
					else
						systemLogger().log(INFO, String.format("[REPLICATION] Pod-Shard (%s, SECONDARY, %s) undeploying", domain, shardId));
					
					Queue<ActorId> queue = system.getPodDomains().get(domain);
					int count=0;
					if (queue!=null) {
						Iterator<ActorId> iterator = queue.iterator();
						for (; iterator.hasNext() && count<allowed;) {
							ActorId id = iterator.next();
							InternalPodActorCell cell = (InternalPodActorCell)id;
							if (!cell.getContext().primaryReplica() && cell.getContext().shardId().equalsIgnoreCase(shardId)) { // does not remove primary replica && same shardId
								systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
								system.send(ActorMessage.create(null, STOP, system.SYSTEM_ID(), id));
								iterator.remove();
								count++;
							}
						}
					}
					if (count>0) {
						List<Integer> secondaryShardCounts = new ArrayList<>(podReplicationTuple.podSystemConfiguration().secondaryShardCounts());
						secondaryShardCounts.set(index, secondaryShardCounts.get(index)-count);
		
						PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
								podReplicationTuple.podSystemConfiguration().primaryShardIds(), 
								podReplicationTuple.podSystemConfiguration().secondaryShardIds(), 
								secondaryShardCounts, 
								podReplicationTuple.podSystemConfiguration().currentShardCount(), 
								0);
						podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration, podReplicationTuple.jarFileName()));
					}
				}
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void undeployPods(String domain) {
		lock.lock();
		try {
			systemLogger().log(INFO, String.format("[REPLICATION] Domain '%s' undeploying", domain));
			
			Queue<ActorId> queue = system.getPodDomains().get(domain);
			if (queue!=null) {
				Iterator<ActorId> iterator = queue.iterator();
				while (iterator.hasNext()) {
					ActorId id = iterator.next();
					systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
					system.send(ActorMessage.create(null, STOP, system.SYSTEM_ID(), id));
					iterator.remove();
				}
			}
			system.getPodDomains().remove(domain);
			podReplicationMap.remove(domain);
			registry.unregister(domain);
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void updatePods(File jarFile, PodConfiguration podConfiguration) {
		updatePods(podConfiguration.domain(), () -> deployPods(jarFile, podConfiguration));
	}
	
	@Override
	public void updatePods(String domain, PodFactory factory, PodConfiguration podConfiguration) {
		updatePods(podConfiguration.domain(), () -> deployPods(factory, podConfiguration));
	}
	
	protected void updatePods(String domain, Procedure deployPods) {
		lock.lock();
		try {
			systemLogger().log(INFO, String.format("[REPLICATION] Domain '%s' updating", domain));
			
			ActorGroup oldPods = new ActorGroupSet();
			Queue<ActorId> queue = system.getPodDomains().get(domain);
			Iterator<ActorId> iterator = queue.iterator();
			while (iterator.hasNext()) {
				ActorId id = iterator.next();
				oldPods.add(id);
				iterator.remove();
			}
			system.getPodDomains().remove(domain);
			
			if (deployPods!=null)
				deployPods.apply();
			
			if (oldPods.size()>0) {
				systemLogger().log(INFO, String.format("[REPLICATION] Outdated PodActor(s) (%s, %s) stopping", domain, oldPods));
				system.broadcast(ActorMessage.create(null, STOP, system.SYSTEM_ID(), null), oldPods);
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void updateReplicaLimits(String domain, int minReplica, int maxReplica) {
		lock.lock();
		try {
			PodReplicationTuple tuple = podReplicationMap.get(domain);
			if (tuple!=null) {
				PodConfiguration oldConfig = tuple.podConfiguration();
				PodConfiguration config = new PodConfiguration(oldConfig.domain(), oldConfig.className(), oldConfig.shardCount(), minReplica, maxReplica, oldConfig.versionNumber()); // validates
				podReplicationMap.put(domain, new PodReplicationTuple(config, tuple.podSystemConfiguration(), tuple.jarFileName()));
				systemLogger().log(INFO, String.format("[REPLICATION] Domain '%s' replica limits %d-%d", domain, minReplica, maxReplica));
				
				// adjust to the new limits (replicas including the primary, per shard)
				if (oldConfig.shardCount()==1)
					adjust(domain, null, tuple.podSystemConfiguration().currentReplicaCount(), minReplica, maxReplica);
				else
					for (int i=0; i<oldConfig.shardCount(); i++)
						adjust(domain, String.valueOf(i), tuple.podSystemConfiguration().secondaryShardCounts().get(i)+1, minReplica, maxReplica);
			}
		}
		finally {
			lock.unlock();
		}
	}

	protected void adjust(String domain, String shardId, int replicas, int minReplica, int maxReplica) {
		if (replicas<minReplica)
			increasePods(domain, shardId, minReplica-replicas);
		else if (replicas>maxReplica)
			decreasePods(domain, shardId, replicas-maxReplica);
	}
	
	protected static PodSystemConfiguration scalingAlgorithm(PodConfiguration podConfiguration) {
		PodSystemConfiguration result = null;
		
		int replicaCount = podConfiguration.minReplica();
		int shardCount = podConfiguration.shardCount();
		
		if (shardCount>1) {
			List<String> primaryShardIds = new ArrayList<>(shardCount);
			List<String> secondaryShardIds = new ArrayList<>(shardCount);
			List<Integer> secondaryShardCounts = new ArrayList<>(shardCount);
			for (int i=0; i<shardCount; i++) {
				primaryShardIds.add(String.valueOf(i));
				secondaryShardIds.add(String.valueOf(i));
				secondaryShardCounts.add(replicaCount-1);
			}
			result = new PodSystemConfiguration(List.copyOf(primaryShardIds), List.copyOf(secondaryShardIds), secondaryShardCounts, shardCount, 0);
		}
		else
			result = new PodSystemConfiguration(null, null, null, 1, replicaCount);
		
		return result;
	}

	@Override
	public void increasePods(String domain, String shardId) {
		increasePods(domain, shardId, 1);
	}
	
	@Override
	public void increasePods(String domain, String shardId, int instances) {
		lock.lock();
		try {
			PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
			
			if (podReplicationTuple!=null) {
				if (podReplicationTuple.jarFileName()!=null)
					systemLogger().log(ERROR, String.format("[REPLICATION] Domain '%s'cannot be deployed (not implemented)", domain));
				else if (podReplicationTuple.podConfiguration().shardCount()==1) {
					int currentReplicaCount = podReplicationTuple.podSystemConfiguration().currentReplicaCount();
					
					int allowed = Math.min(instances, podReplicationTuple.podConfiguration().maxReplica()-currentReplicaCount);
					if (allowed>0) {
						PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
								null, null, null, 1, podReplicationTuple.podSystemConfiguration().currentReplicaCount()+allowed);
						podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration, podReplicationTuple.jarFileName()));
						
						PodDeployment.increasePods((PodFactory)registry.getFactory(domain), podReplicationTuple.podConfiguration(), podSystemConfiguration, allowed, null, system, hasPrimaryReplica);
					}
					if (allowed<instances)
						systemLogger().log(WARN, String.format("[REPLICATION] Domain '%s': limited to maxReplica (%d)", domain, podReplicationTuple.podConfiguration().maxReplica()));
				}
				else if (shardId==null || !podReplicationTuple.podSystemConfiguration().secondaryShardIds().contains(shardId))
					systemLogger().log(WARN, String.format("[REPLICATION] Domain '%s': unknown shard (%s)", domain, shardId));
				else {
					int index = Integer.parseInt(shardId);
					List<Integer> secondaryShardCounts = new ArrayList<>(podReplicationTuple.podSystemConfiguration().secondaryShardCounts());
					
					int allowed = Math.min(instances, podReplicationTuple.podConfiguration().maxReplica()-(secondaryShardCounts.get(index)+1));
					if (allowed>0) {
						secondaryShardCounts.set(index, secondaryShardCounts.get(index)+allowed);
						PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
								podReplicationTuple.podSystemConfiguration().primaryShardIds(), 
								podReplicationTuple.podSystemConfiguration().secondaryShardIds(), 
								secondaryShardCounts, 
								podReplicationTuple.podSystemConfiguration().currentShardCount(), 
								0);
						podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration, podReplicationTuple.jarFileName()));
						
						PodDeployment.increasePods((PodFactory)registry.getFactory(domain), podReplicationTuple.podConfiguration(), podSystemConfiguration, allowed, shardId, system, hasPrimaryReplica);
					}
					if (allowed<instances)
						systemLogger().log(WARN, String.format("[REPLICATION] Shard (%s, %s): limited to maxReplica (%d)", domain, shardId, podReplicationTuple.podConfiguration().maxReplica()));
				}
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void decreasePods(String domain, String shardId) {
		decreasePods(domain, shardId, 1);
	}
	
	@Override
	public void decreasePods(String domain, String shardId, int instances) {
		lock.lock();
		try {
			PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
			
			if (podReplicationTuple!=null) {
				if (podReplicationTuple.jarFileName()!=null)
					systemLogger().log(ERROR, String.format("[REPLICATION] Domain '%s'cannot be undeployed (not implemented)", domain));
				else 
					undeployPod(domain, shardId, instances);
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void podStopped(String domain, String shardId, boolean primaryReplica) {
		lock.lock();
		try {
			PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
			
			if (podReplicationTuple!=null) {
				if (podReplicationTuple.podConfiguration().shardCount()==1) {
					if (primaryReplica)
						systemLogger().log(WARN, String.format("[REPLICATION] Domain '%s': primary replica stopped", domain));
					else
						systemLogger().log(WARN, String.format("[REPLICATION] Domain '%s': pod stopped unexpectedly", domain));
					
					PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
							null, null, null, 1, podReplicationTuple.podSystemConfiguration().currentReplicaCount()-1);
					podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration, podReplicationTuple.jarFileName()));
				}
				else if (primaryReplica)
					systemLogger().log(WARN, String.format("[REPLICATION] Shard (%s, %s): primary replica stopped", domain, shardId));
				else if (shardId!=null && podReplicationTuple.podSystemConfiguration().secondaryShardIds().contains(shardId)) {
					systemLogger().log(WARN, String.format("[REPLICATION] Shard (%s, %s): secondary pod stopped unexpectedly", domain, shardId));
					
					int index = Integer.parseInt(shardId);
					List<Integer> secondaryShardCounts = new ArrayList<>(podReplicationTuple.podSystemConfiguration().secondaryShardCounts());
					secondaryShardCounts.set(index, secondaryShardCounts.get(index)-1);
					
					PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
							podReplicationTuple.podSystemConfiguration().primaryShardIds(), 
							podReplicationTuple.podSystemConfiguration().secondaryShardIds(), 
							secondaryShardCounts, 
							podReplicationTuple.podSystemConfiguration().currentShardCount(), 
							0);
					podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration, podReplicationTuple.jarFileName()));
				}
			}
		}
		finally {
			lock.unlock();
		}
	}
}
