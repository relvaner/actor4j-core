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

package io.actor4j.core.internal.pods;

import static io.actor4j.core.logging.ActorLogger.*;

import java.util.List;

import io.actor4j.core.ActorPodService;
import io.actor4j.core.pods.Pod;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.PodFactory;

public class PodDeployment {
	public static void deployPods(PodFactory factory, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, ActorPodService service) {
		systemLogger().log(INFO, String.format("[REPLICATION] Domain '%s' deploying", podConfiguration.domain()));
		
		if (podSystemConfiguration.currentShardCount()==1)
			deployPods(factory, podSystemConfiguration.currentReplicaCount(), podConfiguration.domain(), service);
		else
			deployPodsAsShards(factory, podConfiguration, podSystemConfiguration, service);		
	}
	
	public static void deployPods(PodFactory factory, int instances, String domain, ActorPodService service) {
		if (factory!=null) {
			boolean primaryReplica = true;
			for (int i=0; i<instances; i++) {
				Pod pod = factory.create();
				pod.register(service, 
						new PodContext(
							domain,
							false,
							null,
							primaryReplica
						));
				if (i==0)
					primaryReplica = false;
				systemLogger().log(INFO, String.format("[REPLICATION] Pod (%s, %s) deployed", domain, pod.getClass().getName()));
			}			
		}
	}
	
	public static void increasePods(PodFactory factory, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, int instances, String shardId, ActorPodService service) {
		systemLogger().log(INFO, String.format("[REPLICATION] Domain '%s' deploying", podConfiguration.domain()));
		
		if (podSystemConfiguration.currentShardCount()==1)
			increasePods(factory, instances, podConfiguration.domain(), service);
		else
			increasePodsAsShards(factory, instances, podConfiguration.domain(), shardId, service);
	}
	
	public static void increasePods(PodFactory factory, int instances, String domain, ActorPodService service) {
		if (factory!=null) {
			for (int i=0; i<instances; i++) {
				Pod pod = factory.create();
				pod.register(service, 
						new PodContext(
							domain,
							false,
							null,
							false
						));
				systemLogger().log(INFO, String.format("[REPLICATION] Pod (%s, %s) deployed", domain, pod.getClass().getName()));
			}			
		}
	}
	
	public static void increasePodsAsShards(PodFactory factory, int instances, String domain, String shardId, ActorPodService service) {
		if (factory!=null) {
			for (int i=0; i<instances; i++) {
				Pod pod = factory.create();
				pod.register(service, 
						new PodContext(
							domain,
							true,
							shardId,
							false
						));
				systemLogger().log(INFO, String.format("[REPLICATION] Pod-Shard (%s, %s, SECONDARY, %s) deployed", domain, pod.getClass().getName(), shardId));
			}			
		}
	}

	public static void deployPodsAsShards(PodFactory factory, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, ActorPodService service) {
		if (factory!=null) {
			List<String> primaryShardIds = podSystemConfiguration.primaryShardIds();
			if (primaryShardIds!=null)
				for (int i=0; i<primaryShardIds.size(); i++) {
					Pod pod = factory.create();
					pod.register(service, 
							new PodContext(
								podConfiguration.domain(), 
								true,
								primaryShardIds.get(i),
								true
							));
					systemLogger().log(INFO, String.format("[SHARDING] Pod-Shard (%s, %s, PRIMARY, %s) deployed", podConfiguration.domain(), pod.getClass().getName(), primaryShardIds.get(i)));
				}
			List<String> secondaryShardIds = podSystemConfiguration.secondaryShardIds();
			if (secondaryShardIds!=null)
				for (int i=0; i<secondaryShardIds.size(); i++) {
					int count = podSystemConfiguration.secondaryShardCounts().get(i);
					if (count>0)
						for (int j=0; j<count; j++) {
							Pod pod = factory.create();
							pod.register(service,
									new PodContext(
										podConfiguration.domain(), 
										true,
										secondaryShardIds.get(i),
										false
									));
							systemLogger().log(INFO, String.format("[REPLICATION] Pod-Shard (%s, %s, SECONDARY, %s) deployed", podConfiguration.domain(), pod.getClass().getName(), secondaryShardIds.get(i)));
						}
				}
		}	
	}
}
