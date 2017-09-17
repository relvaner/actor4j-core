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
package actor4j.core.utils;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class HubPattern {
	protected Actor actor;
	
	protected ActorGroupSet ports;

	public HubPattern(Actor actor) {
		super();
		
		this.actor = actor;
		
		ports = new ActorGroupSet();
	}
	
	public HubPattern(Actor actor, ActorGroup group) {
		this(actor);
		
		ports.addAll(group);
	}
	
	public ActorGroup getPorts() {
		return ports;
	}

	public void add(UUID id) {
		ports.add(id);
	}
	
	public void addAll(ActorGroup group) {
		ports.addAll(group);
	}
	
	public void remove(UUID id) {
		ports.remove(id);
	}
	
	public boolean contains(UUID id) {
		return ports.contains(id);
	}
	
	public int count() {
		return ports.size();
	}
	
	public void broadcast(ActorMessage<?> message) {
		for (UUID dest : ports)
			actor.send(message, dest);
	}
}
