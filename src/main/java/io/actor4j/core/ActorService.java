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

import java.util.List;
import java.util.UUID;

import io.actor4j.core.config.ActorServiceConfig;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;

public class ActorService extends ActorSystem {
	public ActorService() {
		super(ActorServiceConfig.create());
	}
	
	public ActorService(ActorSystemImplFactory factory) {
		this(factory, null);
	}
	
	public ActorService(ActorServiceConfig config) {
		super(config!=null ? config : ActorServiceConfig.create());
	}
	
	public ActorService(ActorSystemImplFactory factory, ActorServiceConfig config) {
		super(factory, config!=null ? config : ActorServiceConfig.create());
	}
	
	@Deprecated
	@Override
	public boolean setConfig(ActorSystemConfig config) {
		return false;
	}
	
	public boolean setConfig(ActorServiceConfig config) {
		return super.setConfig(config);
	}

	public boolean hasActor(String uuid) {
		return system.hasActor(uuid);
	}
	
	public UUID getActorFromAlias(String alias) {
		return system.getActorFromAlias(alias);
	}
	
	public List<UUID> getActorsFromAlias(String alias) {
		return system.getActorsFromAlias(alias);
	}
	
	public boolean sendViaAliasAsServer(ActorMessage<?> message, String alias) {
		return system.sendViaAliasAsServer(message, alias);
	}
	
	public void sendAsServer(ActorMessage<?> message) {
		system.sendAsServer(message);
	}
}
