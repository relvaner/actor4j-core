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
	
	public UUID USER_ID();
	public UUID SYSTEM_ID();
	
	public ActorSystemConfig getConfig();
	public boolean setConfig(ActorSystemConfig config);
	
	public UUID addActor(ActorFactory factory);
	public List<UUID> addActor(ActorFactory factory, int instances);
	
	public void deployPods(File jarFile, PodConfiguration podConfiguration);
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration);
	public void undeployPods(String domain);
	
	public ActorSystem setAlias(UUID id, String alias);
	public ActorSystem setAlias(List<UUID> ids, String alias);
	public UUID getActorFromAlias(String alias);
	public List<UUID> getActorsFromAlias(String alias);
	public String getAliasFromActor(UUID id);
	public String getActorPath(UUID uuid);
	public UUID getActorFromPath(String path);
	
	public ActorSystem send(ActorMessage<?> message);
	public ActorSystem sendViaPath(ActorMessage<?> message, String path);
	public ActorSystem sendViaAlias(ActorMessage<?> message, String alias);
	public ActorSystem sendWhenActive(ActorMessage<?> message);
	public ActorSystem broadcast(ActorMessage<?> message, ActorGroup group);
	
	public UUID getRedirectionDestination(UUID source);
	public ActorSystem addRedirection(UUID source, UUID dest);
	public ActorSystem removeRedirection(UUID source);
	public ActorSystem clearRedirections();
	
	public ActorTimer timer();
	public ActorTimer globalTimer();
	
	public boolean start();
	public boolean start(Runnable onStartup, Runnable onTermination);
	public void shutdownWithActors();
	public void shutdownWithActors(final boolean await);
	public void shutdown();
	public void shutdown(boolean await);
}
