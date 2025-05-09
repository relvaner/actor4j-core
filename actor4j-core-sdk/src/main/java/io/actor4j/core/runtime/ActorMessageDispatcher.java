/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public abstract class ActorMessageDispatcher {
	protected final InternalActorSystem system;
	
	public ActorMessageDispatcher(InternalActorSystem system) {
		super();
		
		this.system = system;
	}
	
	public ActorId ALIAS_ID() {
		return system.ALIAS_ID();
	}
	
	public void unsafe_post(ActorMessage<?> message, ActorId source) {
		unsafe_post(message, source, null);
	}
	
	public abstract void unsafe_post(ActorMessage<?> message, ActorId source, String alias);

	public void post(ActorMessage<?> message, ActorId source) {
		post(message, source, null);
	}
	
	public abstract void post(ActorMessage<?> message, ActorId source, String alias);
	
	public abstract void postOuter(ActorMessage<?> message);
	
	public abstract void postServer(ActorMessage<?> message);
	
	public abstract void postPriority(ActorMessage<?> message);
	
	public abstract void postDirective(ActorMessage<?> message);
	
	public abstract void postPersistence(ActorMessage<?> message);
	
	public abstract void undelivered(ActorMessage<?> message, ActorId source, ActorId dest);
	
	public abstract void registerCell(InternalActorCell cell);
	
	public abstract void unregisterCell(InternalActorCell cell);
	
	public abstract boolean isRegisteredCell(InternalActorCell cell);
}
