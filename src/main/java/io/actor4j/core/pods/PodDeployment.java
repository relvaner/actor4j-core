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

import static io.actor4j.core.utils.ActorLogger.systemLogger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import io.actor4j.core.ActorPodService;

public class PodDeployment {
	public static void deployPods(File jarFile, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, ActorPodService service) {
		systemLogger().info(String.format("[REPLICATION] Domain '%s' deploying", podConfiguration.getDomain()));
		
		Class<?> clazz;
		try {
			clazz = Class.forName(podConfiguration.getClassName(), false, new URLClassLoader(new URL[] { jarFile.toURI().toURL() }));

			if (podSystemConfiguration.getCurrentShardCount()==1)
				deployPods(clazz, podSystemConfiguration.currentReplicaCount, podConfiguration.getDomain(), service);
			else
				deployPodsAsShards(clazz, podConfiguration, podSystemConfiguration, service);
		} catch (ClassNotFoundException | MalformedURLException e) {
			e.printStackTrace();
		}		
	}
	
	public static void deployPods(Class<?> clazz, int instances, String domain, ActorPodService service) {
		if (clazz!=null)
			try {
				boolean primaryReplica = true;
				for (int i=0; i<instances; i++) {
					Object pod = clazz.getConstructor().newInstance();
					clazz.getMethod("register", ActorPodService.class, PodContext.class).invoke(pod, service, 
							new PodContext(
									domain,
									false,
									null,
									primaryReplica
							));
					if (i==0)
						primaryReplica = false;
					systemLogger().info(String.format("[REPLICATION] Pod (%s, %s) deployed", domain, clazz.getName()));
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}	
	}

	public static void deployPodsAsShards(Class<?> clazz, PodConfiguration podConfiguration, PodSystemConfiguration podSystemConfiguration, ActorPodService service) {
		if (clazz!=null)
			try {
				List<String> primaryShardIds = podSystemConfiguration.getPrimaryShardIds();
				if (primaryShardIds!=null)
					for (int i=0; i<primaryShardIds.size(); i++) {
						Object pod = clazz.getConstructor().newInstance();
						clazz.getMethod("register", ActorPodService.class, PodContext.class).invoke(pod, service, 
								new PodContext(
									podConfiguration.getDomain(), 
									true,
									primaryShardIds.get(i),
									true
								));
						systemLogger().info(String.format("[REPLICATION] Pod-Shard (%s, %s, PRIMARY, %s) deployed", podConfiguration.getDomain(), clazz.getName(), primaryShardIds.get(i)));
					}
				List<String> secondaryShardIds = podSystemConfiguration.getSecondaryShardIds();
				if (secondaryShardIds!=null)
					for (int i=0; i<secondaryShardIds.size(); i++) {
						int count = podSystemConfiguration.getSecondaryShardCounts().get(i);
						if (count>0)
							for (int j=0; j<count; j++) {
								Object pod = clazz.getConstructor().newInstance();
								clazz.getMethod("register", ActorPodService.class, PodContext.class).invoke(pod, service, 
										new PodContext(
											podConfiguration.getDomain(), 
											true,
											secondaryShardIds.get(i),
											false
										));
								systemLogger().info(String.format("[REPLICATION] Pod-Shard (%s, %s, SECONDARY, %s) deployed", podConfiguration.getDomain(), clazz.getName(), secondaryShardIds.get(i)));
							}
					}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}	
	}
}
