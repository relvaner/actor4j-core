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
package io.actor4j.core.runtime.classic;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import io.actor4j.core.runtime.AbstractActorProcessPool;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.DefaultActorProcessPoolHandler;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;

public class ActorRunnablePool extends AbstractActorProcessPool<ActorRunnable> {
	protected final ExecutorService executorService;
	
	public ActorRunnablePool(InternalActorRuntimeSystem system) {
		super(system, new ActorRunnablePoolHandler(system));
		
		int poolSize = system.getConfig().parallelism()*system.getConfig().parallelismFactor();
		//slow // executorService = new ThreadPoolExecutor(poolSize, poolSize, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory(system.getConfig().name()+ "-worker-thread"));
		executorService = new ForkJoinPool(poolSize);
		
		for (int i=0; i<poolSize; i++)
			actorProcessList.add(new DefaultActorRunnable(system, i));
		
		((DefaultActorProcessPoolHandler<ActorRunnable>)actorProcessPoolHandler).beforeStart(actorProcessList);
	}
	
	public void submit(Runnable runnable, UUID dest) {
		if (!executorService.isShutdown())
			try {
				executorService.submit(runnable);
			}
			catch (RejectedExecutionException e) {
				system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.EXECUTER_ACTOR, dest);
			}
	}

	@Override
	public void shutdown(Runnable onTermination, boolean await) {
		executorService.shutdownNow();
		if (await)
			try {
				executorService.awaitTermination(system.getConfig().awaitTerminationTimeout(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	public ActorRunnablePoolHandler getActorRunnablePoolHandler() {
		return (ActorRunnablePoolHandler)actorProcessPoolHandler;
	}
}
