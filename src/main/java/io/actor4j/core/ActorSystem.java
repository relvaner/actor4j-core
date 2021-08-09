/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
import io.actor4j.core.internal.ActorSystemImpl;
import io.actor4j.core.internal.DefaultActorSystemImpl;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorTimer;

public class ActorSystem {
	protected ActorSystemImpl system;
	
	public final UUID USER_ID;
	public final UUID SYSTEM_ID;
	
	public ActorSystem() {
		this((wrapper, c) -> new DefaultActorSystemImpl(wrapper, c), null);
	}
	
	public ActorSystem(ActorSystemImplFactory factory) {
		this(factory, null);
	}
	
	public ActorSystem(ActorSystemConfig config) {
		this((wrapper, c) -> new DefaultActorSystemImpl(wrapper, c), config);
	}
	
	public ActorSystem(ActorSystemImplFactory factory, ActorSystemConfig config) {
		super();
		
		system = factory.apply(this, config);
		
		USER_ID    = system.USER_ID;
		SYSTEM_ID  = system.SYSTEM_ID;
	}
	
	public ActorSystemConfig getConfig() {
		return system.getConfig();
	}
	
	public UUID addActor(ActorFactory factory) {
		return system.addActor(factory);
	}
	
	public List<UUID> addActor(ActorFactory factory, int instances) {
		return system.addActor(factory, instances);
	}
	
	public void deployPods(File jarFile, PodConfiguration podConfiguration) {
		system.deployPods(jarFile, podConfiguration);
	}
	
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration) {
		system.deployPods(factory, podConfiguration);
	}
	
	public void undeployPods(String domain) {
		system.undeployPods(domain);
	}
	
	public ActorSystem setAlias(UUID id, String alias) {
		system.setAlias(id, alias);
		
		return this;
	}
	
	public ActorSystem setAlias(List<UUID> ids, String alias) {
		system.setAlias(ids, alias);
		
		return this;
	}
	
	public UUID getActorFromAlias(String alias) {
		return system.getActorFromAlias(alias);
	}
	
	public List<UUID> getActorsFromAlias(String alias) {
		return system.getActorsFromAlias(alias);
	}
	
	public String getActorPath(UUID uuid) {
		return system.getActorPath(uuid);
	}
	
	public UUID getActorFromPath(String path) {
		return system.getActorFromPath(path);
	}
		
	public ActorSystem send(ActorMessage<?> message) {
		system.send(message);
		
		return this;
	}
	
	public ActorSystem sendViaPath(ActorMessage<?> message, String path) {
		system.sendViaPath(message, path);
		
		return this;
	}
	
	public ActorSystem sendViaAlias(ActorMessage<?> message, String alias) {
		system.sendViaAlias(message, alias);
		
		return this;
	}
	
	public ActorSystem sendWhenActive(ActorMessage<?> message) {
		system.sendWhenActive(message);
		
		return this;
	}
	
	public ActorSystem broadcast(ActorMessage<?> message, ActorGroup group) {
		system.broadcast(message, group);
		
		return this;
	}
	
	public UUID getRedirectionDestination(UUID source) {
		return system.getRedirectionDestination(source);
	}
	
	public ActorSystem addRedirection(UUID source, UUID dest) {
		system.addRedirection(source, dest);
		
		return this;
	}
	
	public ActorSystem removeRedirection(UUID source) {
		system.removeRedirection(source);
		
		return this;
	}
	
	public ActorSystem clearRedirections() {
		system.clearRedirections();
		
		return this;
	}
	
	public ActorTimer timer() {
		return system.timer();
	}
	
	public ActorTimer globalTimer() {
		return system.globalTimer();
	}
	
	public void start() {
		system.start();
	}
	
	public void start(Runnable onStartup, Runnable onTermination) {
		system.start(onStartup, onTermination);
	}
	
	public void shutdownWithActors() {
		system.shutdownWithActors();
	}
	
	public void shutdownWithActors(final boolean await) {
		system.shutdownWithActors(await);
	}
	
	public void shutdown() {
		system.shutdown();
	}
	
	public void shutdown(boolean await) {
		system.shutdown(await);
	}
	
	public ActorSystemImpl underlyingImpl() {
		return system;
	}
}
