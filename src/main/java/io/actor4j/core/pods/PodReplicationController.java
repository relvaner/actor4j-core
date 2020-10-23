/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.actor4j.core.ActorSystemImpl;
import io.actor4j.core.function.Procedure;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;

import static io.actor4j.core.utils.ActorLogger.*;
import static io.actor4j.core.actors.Actor.*;

public class PodReplicationController {
	protected final ActorSystemImpl system;
	
	protected final Map<String, PodReplicationTuple> podReplicationMap;
	
	public PodReplicationController(ActorSystemImpl system) {
		super();
		this.system = system;
		
		podReplicationMap = new ConcurrentHashMap<>();
	}

	public void deployPods(File jarFile, PodConfiguration podConfiguration) {
		PodSystemConfiguration podSystemConfiguration = scalingAlgorithm(podConfiguration);
		podReplicationMap.put(podConfiguration.domain, new PodReplicationTuple(podConfiguration, podSystemConfiguration, jarFile.getAbsolutePath()));
		if (podSystemConfiguration!=null)
			PodDeployment.deployPods(jarFile, podConfiguration, podSystemConfiguration, system);
	}
	
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration) {
		PodSystemConfiguration podSystemConfiguration = scalingAlgorithm(podConfiguration);
		podReplicationMap.put(podConfiguration.domain, new PodReplicationTuple(podConfiguration, podSystemConfiguration));
		if (podSystemConfiguration!=null)
			PodDeployment.deployPods(factory, podConfiguration, podSystemConfiguration, system);
	}
	
	public void undeployPods(String domain) {
		systemLogger().info(String.format("[REPLICATION] Domain '%s' undeploying", domain));
		
		Queue<UUID> queue = system.getPodDomains().get(domain);
		Iterator<UUID> iterator = queue.iterator();
		while (iterator.hasNext()) {
			UUID id = iterator.next();
			systemLogger().info(String.format("[REPLICATION] PodActor (%s, %s) stopping", domain, id));
			system.send(new ActorMessage<>(null, STOP, system.SYSTEM_ID, id));
			iterator.remove();
		}
		system.getPodDomains().remove(domain);
	}
	
	public void updatePods(File jarFile, PodConfiguration podConfiguration) {
		updatePods(podConfiguration.domain, () -> deployPods(jarFile, podConfiguration));
	}
	
	public void updatePods(String domain, PodFactory factory, PodConfiguration podConfiguration) {
		updatePods(podConfiguration.domain, () -> deployPods(factory, podConfiguration));
	}
	
	protected void updatePods(String domain, Procedure deployPods) {
		systemLogger().info(String.format("[REPLICATION] Domain '%s' updating", domain));
		
		ActorGroup oldPods = new ActorGroupSet();
		Queue<UUID> queue = system.getPodDomains().get(domain);
		Iterator<UUID> iterator = queue.iterator();
		while (iterator.hasNext()) {
			UUID id = iterator.next();
			oldPods.add(id);
			iterator.remove();
		}
		system.getPodDomains().remove(domain);
		
		if (deployPods!=null)
			deployPods.apply();
		
		if (oldPods.size()>0) {
			systemLogger().info(String.format("[REPLICATION] Outdated PodActor(s) (%s, %s) stopping", domain, oldPods));
			system.broadcast(new ActorMessage<Object>(null, STOP, system.SYSTEM_ID, null), oldPods);
		}
	}
	
	public static PodSystemConfiguration scalingAlgorithm(PodConfiguration podConfiguration) {
		PodSystemConfiguration result = null;
		
		int replicaCount = podConfiguration.minReplica;
		
		if (podConfiguration.getShardCount()>1) {
			List<String> primaryShardIds = new LinkedList<>();
			for (int i=0; i<podConfiguration.getShardCount(); i++)
				primaryShardIds.add(String.valueOf(i));
			
			if (podConfiguration.getMinReplica()==1) {	
				result = new PodSystemConfiguration(primaryShardIds, null, null, podConfiguration.getShardCount(), 0);
			}
			else {
				List<String> secondaryShardIds = new LinkedList<>();
				List<Integer> secondaryShardCounts = new LinkedList<>();
				for (int i=0; i<podConfiguration.getShardCount(); i++) {
					secondaryShardIds.add(String.valueOf(i));
					secondaryShardCounts.add(replicaCount-1);
				}
				result = new PodSystemConfiguration(primaryShardIds, secondaryShardIds, secondaryShardCounts, podConfiguration.getShardCount(), 0);
			}
		}
		else {
			result = new PodSystemConfiguration(null, null, null, 1, replicaCount);
		}
		
		return result;
	}
	
	public Map<String, PodReplicationTuple> getPodReplicationMap() {
		return podReplicationMap;
	}

	public void increasePods(String domain) {
		
	}
	
	public void decreasePods(String domain) {
		
	}
}
