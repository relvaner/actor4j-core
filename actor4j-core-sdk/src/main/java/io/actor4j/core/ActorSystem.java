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
package io.actor4j.core;

import java.io.File;
import java.util.List;
import java.util.UUID;

import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorTimer;

public interface ActorSystem {
	public static ActorSystem create(ActorSystemFactory factory, String name) {
		return create(factory, ActorSystemConfig.builder().name(name).build());
	}
	
	public static ActorSystem create(ActorSystemFactory factory) {
		return create(factory, ActorSystemConfig.create());
	}
	
	public static ActorSystem create(ActorSystemFactory factory, ActorSystemConfig config) {
		return factory.apply(config);
	}
	
	public ActorId USER_ID();
	public ActorId SYSTEM_ID();
	
	public ActorSystemConfig getConfig();
	public boolean setConfig(ActorSystemConfig config);
	
	public ActorId addActor(ActorFactory factory);
	public List<ActorId> addActor(ActorFactory factory, int instances);
	
	public ActorId deployActor(ActorFactory factory);
	public void undeployActor(ActorId id);
	public void undeployActors(String alias);
	
	public void deployPods(File jarFile, PodConfiguration podConfiguration);
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration);
	public void undeployPods(String domain);

	public ActorId getActor(UUID globalId);
	public ActorSystem setAlias(ActorId id, String alias);
	public ActorSystem setAlias(List<ActorId> ids, String alias);
	public ActorId getActorFromAlias(String alias);
	public List<ActorId> getActorsFromAlias(String alias);
	public String getAliasFromActor(ActorId id);
	public String getActorPath(ActorId id);
	public ActorId getActorFromPath(String path);
	
	public ActorSystem send(ActorMessage<?> message);
	public ActorSystem send(ActorMessage<?> message, ActorId dest);
	public ActorSystem sendViaPath(ActorMessage<?> message, String path);
	public ActorSystem sendViaAlias(ActorMessage<?> message, String alias);
	public ActorSystem sendWhenActive(ActorMessage<?> message);
	public ActorSystem sendViaGlobalId(ActorMessage<?> message, UUID globalId);
	public ActorSystem broadcast(ActorMessage<?> message, ActorGroup group);
	
	public ActorId getRedirectionDestination(ActorId source);
	public ActorSystem addRedirection(ActorId source, ActorId dest);
	public ActorSystem removeRedirection(ActorId source);
	
	public ActorTimer timer();
	public ActorTimer globalTimer();
	
	public boolean start();
	public boolean start(Runnable onStartup, Runnable onTermination);
	public void shutdownWithActors();
	public void shutdownWithActors(final boolean await);
	public void shutdown();
	public void shutdown(boolean await);
}
