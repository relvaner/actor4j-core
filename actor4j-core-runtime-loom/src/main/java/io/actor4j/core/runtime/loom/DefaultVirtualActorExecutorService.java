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

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorExecutorServiceImpl;
import io.actor4j.core.runtime.ActorExecutionUnitPool;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;

public class DefaultVirtualActorExecutorService extends ActorExecutorServiceImpl<VirtualActorRunnable> implements InternalVirtualActorExecutorService {
	protected /*quasi final*/ VirtualActorRunnablePool virtualActorResourceRunnablePool;
	
	public DefaultVirtualActorExecutorService(InternalActorRuntimeSystem system) {
		super(system);
	}
	
	@Override
	public void createActorResourcePool(int poolSize) {
		virtualActorResourceRunnablePool = new VirtualActorRunnablePool(system, (pool) -> new DefaultVirtualActorRunnablePoolHandler(system, pool), true);
	}
	
	@Override
	public void shutdownActorResourcePool(boolean await) {
		virtualActorResourceRunnablePool.shutdown(null, await);
	}
	
	@Override
	public void resource(final ActorMessage<?> message) {
		final InternalActorCell cell = system.getCells().get(message.dest());
		if (cell!=null) {
			if (virtualActorResourceRunnablePool.isStarted()) {
				if (message.tag()<0)
					virtualActorResourceRunnablePool.getVirtualActorRunnablePoolHandler().post(message, true, true);
				else
					virtualActorResourceRunnablePool.getVirtualActorRunnablePoolHandler().post(message, false, true);
			}
		}
	}
	
	@Override
	public ActorExecutionUnitPool<VirtualActorRunnable> createExecutionUnitPool() {
		return new VirtualActorRunnablePool(system, (pool) -> new DefaultVirtualActorRunnablePoolHandler(system, pool), false);
	}
	
	@Override
	public VirtualActorRunnablePool getVirtualActorResourceRunnablePool() {
		return (VirtualActorRunnablePool)virtualActorResourceRunnablePool;
	}
	
	@Override
	public VirtualActorRunnablePool getVirtualActorRunnablePool() {
		return (VirtualActorRunnablePool)executionUnitPool;
	}
}
