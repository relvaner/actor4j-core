/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.actor4j.core.function.Procedure;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.InternalPodActorCell;
import io.actor4j.core.runtime.di.DefaultDIContainer;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;

import static io.actor4j.core.actors.Actor.*;
import static io.actor4j.core.logging.ActorLogger.*;

public class DefaultPodReplicationController implements PodReplicationController {
	protected final InternalActorSystem system;
	
	protected final DefaultDIContainer<String> container;
	protected final Map<String, PodReplicationTuple> podReplicationMap;
	
	protected final Function<String, Boolean> hasPrimaryReplica;
	
	public DefaultPodReplicationController(InternalActorSystem system) {
		super();
		this.system = system;
		
		container = new DefaultDIContainer<>();
		podReplicationMap = new ConcurrentHashMap<>();
		
		hasPrimaryReplica = (domain) -> system.primaryPodDeployed(domain);
	}

	@Override
	public void deployPods(File jarFile, PodConfiguration podConfiguration) {
		PodSystemConfiguration podSystemConfiguration = scalingAlgorithm(podConfiguration);
		podReplicationMap.put(podConfiguration.domain(), new PodReplicationTuple(podConfiguration, podSystemConfiguration, jarFile.getAbsolutePath()));
		if (podSystemConfiguration!=null)
			deployPods(jarFile, podConfiguration, podSystemConfiguration, system);
	}
	
	protected void deployPods(File jarFile, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, InternalActorSystem system) {
		systemLogger().log(ERROR, String.format("[REPLICATION] Domain '%s'cannot be deployed (not implemented)", podConfiguration.domain()));
	}
	
	@Override
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration) {
		PodSystemConfiguration podSystemConfiguration = scalingAlgorithm(podConfiguration);
		podReplicationMap.put(podConfiguration.domain(), new PodReplicationTuple(podConfiguration, podSystemConfiguration));
		if (podSystemConfiguration!=null) {
			container.register(podConfiguration.domain(), factory);
			PodDeployment.deployPods(factory, podConfiguration, podSystemConfiguration, system, hasPrimaryReplica);
		}
	}
	
	@Override
	public void undeployPod(String domain, String shardId, int instances) {
		PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
		
		if (podReplicationTuple!=null) {
			if (podReplicationTuple.podConfiguration().shardCount()==1) {
				systemLogger().log(INFO, String.format("[REPLICATION] Pod (%s) undeploying", domain));
				
				Queue<ActorId> queue = system.getPodDomains().get(domain);
				Iterator<ActorId> iterator = queue.iterator();
				int count=0;
				for (; iterator.hasNext() && count<instances;) {
					ActorId id = iterator.next();
					InternalPodActorCell cell = (InternalPodActorCell)id;
					if (!cell.getContext().primaryReplica()) { // does not remove primary replica
						systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
						system.send(ActorMessage.create(null, STOP, system.SYSTEM_ID(), id));
						iterator.remove();
						count++;
					}
				}
				
				PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
						null, null, null, 1, podReplicationTuple.podSystemConfiguration().currentReplicaCount()-count);
				podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration));
			}
			else {
				systemLogger().log(INFO, String.format("[REPLICATION] Pod-Shard (%s, SECONDARY, %s) undeploying", domain, shardId));
				
				Queue<ActorId> queue = system.getPodDomains().get(domain);
				Iterator<ActorId> iterator = queue.iterator();
				int count=0;
				for (; iterator.hasNext() && count<instances;) {
					ActorId id = iterator.next();
					InternalPodActorCell cell = (InternalPodActorCell)id;
					if (!cell.getContext().primaryReplica() && cell.getContext().shardId().equalsIgnoreCase(shardId)) { // does not remove primary replica && same shardId
						systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
						system.send(ActorMessage.create(null, STOP, system.SYSTEM_ID(), id));
						iterator.remove();
						count++;
					}
				}
				
				PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
						podReplicationTuple.podSystemConfiguration().primaryShardIds(), 
						podReplicationTuple.podSystemConfiguration().secondaryShardIds(), 
						podReplicationTuple.podSystemConfiguration().secondaryShardCounts(), 
						podReplicationTuple.podSystemConfiguration().currentShardCount(), 
						0);
				podSystemConfiguration.secondaryShardCounts().set(Integer.valueOf(shardId), podSystemConfiguration.secondaryShardCounts().get(Integer.valueOf(shardId))-count);
				podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration));
			}
		}
	}
	
	@Override
	public void undeployPods(String domain) {
		systemLogger().log(INFO, String.format("[REPLICATION] Domain '%s' undeploying", domain));
		
		Queue<ActorId> queue = system.getPodDomains().get(domain);
		Iterator<ActorId> iterator = queue.iterator();
		while (iterator.hasNext()) {
			ActorId id = iterator.next();
			systemLogger().log(INFO, String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
			system.send(ActorMessage.create(null, STOP, system.SYSTEM_ID(), id));
			iterator.remove();
		}
		system.getPodDomains().remove(domain);
		container.unregister(domain);
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
	
	public static PodSystemConfiguration scalingAlgorithm(PodConfiguration podConfiguration) {
		PodSystemConfiguration result = null;
		
		int replicaCount = podConfiguration.minReplica();
		
		if (podConfiguration.shardCount()>1) {
			List<String> primaryShardIds = new LinkedList<>();
			for (int i=0; i<podConfiguration.shardCount(); i++)
				primaryShardIds.add(String.valueOf(i));
			
			if (replicaCount==1) {	
				result = new PodSystemConfiguration(primaryShardIds, null, null, podConfiguration.shardCount(), 0);
			}
			else {
				List<String> secondaryShardIds = new LinkedList<>();
				List<Integer> secondaryShardCounts = new LinkedList<>();
				for (int i=0; i<podConfiguration.shardCount(); i++) {
					secondaryShardIds.add(String.valueOf(i));
					secondaryShardCounts.add(replicaCount-1);
				}
				result = new PodSystemConfiguration(primaryShardIds, secondaryShardIds, secondaryShardCounts, podConfiguration.shardCount(), 0);
			}
		}
		else {
			result = new PodSystemConfiguration(null, null, null, 1, replicaCount);
		}
		
		return result;
	}
	
	@Override
	public Map<String, PodReplicationTuple> getPodReplicationMap() {
		return podReplicationMap;
	}

	@Override
	public void increasePods(String domain, String shardId) {
		increasePods(domain, shardId, 1);
	}
	
	@Override
	public void increasePods(String domain, String shardId, int instances) {
		PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
		
		if (podReplicationTuple!=null) {
			if (podReplicationTuple.jarFileName()!=null)
				systemLogger().log(ERROR, String.format("[REPLICATION] Domain '%s'cannot be deployed (not implemented)", domain));
			else if (podReplicationTuple.podConfiguration().shardCount()==1) {
				PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
						null, null, null, 1, podReplicationTuple.podSystemConfiguration().currentReplicaCount()+instances);
				podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration));
				
				PodDeployment.increasePods((PodFactory)container.getFactory(domain), podReplicationTuple.podConfiguration(), podSystemConfiguration, instances, null, system, hasPrimaryReplica);
			}
			else {
				PodSystemConfiguration podSystemConfiguration = new PodSystemConfiguration(
						podReplicationTuple.podSystemConfiguration().primaryShardIds(), 
						podReplicationTuple.podSystemConfiguration().secondaryShardIds(), 
						podReplicationTuple.podSystemConfiguration().secondaryShardCounts(), 
						podReplicationTuple.podSystemConfiguration().currentShardCount(), 
						0);
				podSystemConfiguration.secondaryShardCounts().set(Integer.valueOf(shardId), podSystemConfiguration.secondaryShardCounts().get(Integer.valueOf(shardId))+instances);
				podReplicationMap.put(domain, new PodReplicationTuple(podReplicationTuple.podConfiguration(), podSystemConfiguration));
				
				PodDeployment.increasePods((PodFactory)container.getFactory(domain), podReplicationTuple.podConfiguration(), podSystemConfiguration, instances, shardId, system, hasPrimaryReplica);
			}
		}
	}
	
	@Override
	public void decreasePods(String domain, String shardId) {
		decreasePods(domain, shardId, 1);
	}
	
	@Override
	public void decreasePods(String domain, String shardId, int instances) {
		PodReplicationTuple podReplicationTuple = podReplicationMap.get(domain);
		
		if (podReplicationTuple!=null) {
			if (podReplicationTuple.jarFileName()!=null)
				systemLogger().log(ERROR, String.format("[REPLICATION] Domain '%s'cannot be undeployed (not implemented)", domain));
			else 
				undeployPod(domain, shardId, instances);
		}
	}
}
