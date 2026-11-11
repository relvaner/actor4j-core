/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.actor4j.core.config.HybridActorSystemConfig;
import io.actor4j.core.runtime.ActorExecutionUnitPool;
import io.actor4j.core.runtime.ActorThread;
import io.actor4j.core.runtime.ActorThreadPool;
import io.actor4j.core.runtime.DefaultActorExecutorService;
import io.actor4j.core.runtime.DefaultInternalActorRuntimeSystem;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;
import io.actor4j.core.runtime.classic.ActorRunnablePool;
import io.actor4j.core.runtime.classic.ClassicActorCell;
import io.actor4j.core.runtime.classic.ClassicInternalActorExecutorService;

// thread pool of the default runtime as main pool, runnable pool of the classic runtime for pooled actors
public class HybridActorExecutorService extends DefaultActorExecutorService implements ClassicInternalActorExecutorService {
	protected /*quasi final*/ ActorRunnablePool classicRunnablePool;

	public HybridActorExecutorService(InternalActorRuntimeSystem system) {
		super(system);
	}

	@Override
	protected void beforeExecutionUnitPool() {
		// queues of pooled cells created before start (afterwards: ClassicActorCell constructor)
		Function<InternalActorCell, Boolean> configQueues = (cell) -> {
			if (cell instanceof ClassicActorCell classicCell)
				classicCell.configQueues();

			return false;
		};
		system.internal_iterateCell((InternalActorCell)system.UNKNOWN_ID(), configQueues);
		system.internal_iterateCell((InternalActorCell)system.SYSTEM_ID(), configQueues);
		system.internal_iterateCell((InternalActorCell)system.USER_ID(), configQueues);
	}

	// see HybridActorSystemConfig
	public int pooledThreads() {
		return HybridActorSystemConfig.pooledThreads(system.getConfig());
	}
	
	public int threadBoundThreads() {
		return HybridActorSystemConfig.threadBoundThreads(system.getConfig());
	}

	@Override
	protected int watchedExecutionUnits() {
		return threadBoundThreads(); // the shared pool is not watched
	}
	
	@Override
	public ActorExecutionUnitPool<ActorThread> createExecutionUnitPool() {
		classicRunnablePool = new ActorRunnablePool(system, pooledThreads());
		
		return new ActorThreadPool((DefaultInternalActorRuntimeSystem)system, threadBoundThreads());
	}
	
	@Override
	public ActorRunnablePool getRunnablePool() {
		return classicRunnablePool;
	}

	@Override
	public void shutdownExecutionUnitPool(Runnable onTermination, boolean await) {
		if (classicRunnablePool!=null)
			classicRunnablePool.shutdown(null, await);
		super.shutdownExecutionUnitPool(onTermination, await);
	}

	@Override
	public long getCount() {
		return super.getCount() + (classicRunnablePool!=null ? classicRunnablePool.getCount() : 0);
	}

	@Override
	public List<Long> getCounts() {
		List<Long> result = new ArrayList<>(super.getCounts());
		if (classicRunnablePool!=null)
			result.addAll(classicRunnablePool.getCounts());
		return result;
	}
}
