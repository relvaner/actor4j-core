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
package cloud.actor4j.core;

import static cloud.actor4j.core.utils.ActorUtils.*;

import java.util.UUID;

import cloud.actor4j.core.messages.ActorMessage;

public abstract class ActorMessageDispatcher {
	protected final ActorSystemImpl system;
	
	protected static final UUID UUID_ALIAS = UUID_ZERO;
	
	public ActorMessageDispatcher(ActorSystemImpl system) {
		super();
		
		this.system = system;
	}

	public void post(ActorMessage<?> message, UUID source) {
		post(message, source, null);
	}
	
	public abstract void post(ActorMessage<?> message, UUID source, String alias);
	
	public abstract void post(ActorMessage<?> message, ActorServiceNode node, String path);
	
	public abstract void postOuter(ActorMessage<?> message);
	
	public abstract void postServer(ActorMessage<?> message);
	
	public abstract void postPriority(ActorMessage<?> message);
	
	public abstract void postDirective(ActorMessage<?> message);
	
	public abstract void postPersistence(ActorMessage<?> message);
	
	public void registerCell(ActorCell cell) {
		system.executerService.actorThreadPool.actorThreadPoolHandler.registerCell(cell);
	}
	
	public void unregisterCell(ActorCell cell) {
		system.executerService.actorThreadPool.actorThreadPoolHandler.unregisterCell(cell);
	}
	
	public boolean isRegisteredCell(ActorCell cell) {
		return system.executerService.actorThreadPool.actorThreadPoolHandler.isRegisteredCell(cell);
	}
}
