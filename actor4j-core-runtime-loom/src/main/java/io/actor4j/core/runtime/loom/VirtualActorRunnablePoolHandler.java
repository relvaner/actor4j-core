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
package io.actor4j.core.runtime.loom;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorExecutionUnitPoolHandler;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public abstract class VirtualActorRunnablePoolHandler implements ActorExecutionUnitPoolHandler<VirtualActorRunnable> {
	protected final InternalActorSystem system;
	protected final VirtualActorRunnablePool virtualActorRunnablePool;

	protected final Map<ActorId, VirtualActorRunnable> virtualActorRunnables;
	
	public VirtualActorRunnablePoolHandler(InternalActorSystem system, VirtualActorRunnablePool virtualActorRunnablePool) {
		super();
		
		this.system = system;
		this.virtualActorRunnablePool = virtualActorRunnablePool;

		virtualActorRunnables = new ConcurrentHashMap<>();
	}
	
	public boolean post(ActorMessage<?> message, boolean directive, boolean debugUndelivered) {
		VirtualActorRunnable virtualActorRunnable = null;
		if (message.dest()!=null) {
			virtualActorRunnable = virtualActorRunnables.get(message.dest());
			if (virtualActorRunnable!=null) {
				if (directive)
					virtualActorRunnable.directiveQueue().offer(message.copy());
				else
					virtualActorRunnable.outerQueue().offer(message.copy());
				virtualActorRunnable.newMessage(virtualActorRunnablePool.virtualThreads.get(message.dest()));
			}
		}
		
		if (debugUndelivered && virtualActorRunnable==null)
			system.getMessageDispatcher().undelivered(message, message.source(), message.dest());
		
		return virtualActorRunnable!=null;
	}
	
	public boolean post(ActorMessage<?> message, ActorId dest, boolean directive, boolean debugUndelivered) {
		VirtualActorRunnable virtualActorRunnable = null;
		if (dest!=null) {
			virtualActorRunnable = virtualActorRunnables.get(dest);
			if (virtualActorRunnable!=null) {
				if (directive)
					virtualActorRunnable.directiveQueue().offer(message.copy(dest));
				else
					virtualActorRunnable.outerQueue().offer(message.copy(dest));
				virtualActorRunnable.newMessage(virtualActorRunnablePool.virtualThreads.get(dest));
			}
		}
		
		if (debugUndelivered && virtualActorRunnable==null) 
			system.getMessageDispatcher().undelivered(message, message.source(), dest);
		
		return virtualActorRunnable!=null;
	}
	
	@Override
	public void postPersistence(ActorMessage<?> message) {
		List<ActorId> ids = system.getExecutorService().getPersistenceService().persistenceActorIds();
		int index = ThreadLocalRandom.current().nextInt(ids.size());
		system.getExecutorService().getPersistenceService().getService().send(message.copy(ids.get(index)));
	}
	
	public Map<ActorId, VirtualActorRunnable> getVirtualActorRunnables() {
		return virtualActorRunnables;
	}
	
	public abstract VirtualActorRunnable createVirtualActorRunnable(InternalActorSystem system, InternalActorCell cell, Runnable onTermination);
	
	public VirtualActorRunnable registerCell(InternalActorCell cell, Runnable onTermination) {
		VirtualActorRunnable result = createVirtualActorRunnable(system, cell, onTermination);
		virtualActorRunnables.put(cell.getId(), result);
		
		return result;
	}
	
	@Deprecated
	@Override
	public void registerCell(InternalActorCell cell) {
		// Not used!
	}
	
	@Override
	public void unregisterCell(InternalActorCell cell) {
		virtualActorRunnables.remove(cell.getId());
	}
	
	@Override
	public boolean isRegisteredCell(InternalActorCell cell) {
		return virtualActorRunnables.containsKey(cell.getId());
	}
}
