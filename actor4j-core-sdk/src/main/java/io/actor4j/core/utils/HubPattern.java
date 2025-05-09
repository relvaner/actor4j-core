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
package io.actor4j.core.utils;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public class HubPattern {
	protected ActorRef actorRef;
	
	protected ActorGroupSet ports;

	public HubPattern(ActorRef actorRef) {
		super();
		
		this.actorRef = actorRef;
		
		ports = new ActorGroupSet();
	}
	
	public HubPattern(ActorRef actorRef, ActorGroup group) {
		this(actorRef);
		
		ports.addAll(group);
	}
	
	public ActorGroup getPorts() {
		return ports;
	}

	public void add(ActorId id) {
		ports.add(id);
	}
	
	public void addAll(ActorGroup group) {
		ports.addAll(group);
	}
	
	public void remove(ActorId id) {
		ports.remove(id);
	}
	
	public boolean contains(ActorId id) {
		return ports.contains(id);
	}
	
	public int count() {
		return ports.size();
	}
	
	public void broadcast(ActorMessage<?> message) {
		for (ActorId dest : ports)
			actorRef.send(message, dest);
	}
	
	public <T> void broadcast(T value, int tag) {
		for (ActorId dest : ports)
			actorRef.tell(value, tag, dest);
	}
}
