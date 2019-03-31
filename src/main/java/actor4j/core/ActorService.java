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
package actor4j.core;

import java.util.List;
import java.util.UUID;

import actor4j.core.messages.ActorMessage;

public class ActorService extends ActorSystem {
	public ActorService() {
		this(null);
	}
	
	public ActorService(String name) {
		super(name);
		serverMode();
	}
	
	public String getServiceNodeName() {
		return system.getServiceNodeName();
	}

	public void setServiceNodeName(String serviceNodeName) {
		system.setServiceNodeName(serviceNodeName);
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
	
	public ActorClientRunnable getClientRunnable() {
		return system.getClientRunnable();
	}
}
