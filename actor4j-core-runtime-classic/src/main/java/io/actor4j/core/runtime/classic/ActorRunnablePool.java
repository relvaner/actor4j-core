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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import io.actor4j.core.runtime.AbstractActorExecutionUnitPool;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;
import io.actor4j.core.runtime.classic.utils.ClassicForkJoinWorkerThread;
import io.actor4j.core.runtime.classic.utils.ClassicForkJoinWorkerThreadFactory;

public class ActorRunnablePool extends AbstractActorExecutionUnitPool<ActorRunnable> {
	protected final ExecutorService executorService;
	
	protected final ActorRunnable actorRunnable;
	
	public ActorRunnablePool(InternalActorRuntimeSystem system) {
		super(system, null);
		
		int poolSize = system.getConfig().parallelism()*system.getConfig().parallelismFactor();
		executorService = new ForkJoinPool(poolSize, new ClassicForkJoinWorkerThreadFactory(), null, false);
		
		actorRunnable = new DefaultActorRunnable(system);
	}
	
	public void submit(ClassicInternalActorCell cell) {
		if (!executorService.isShutdown())
			try {
				if (Thread.currentThread() instanceof ClassicForkJoinWorkerThread) {
					ForkJoinTask<?> task = ForkJoinTask.adapt(() -> actorRunnable.run(cell));
					task.fork();
				}
				else
					executorService.submit(() -> actorRunnable.run(cell));
			}
			catch (RejectedExecutionException e) {
				system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.EXECUTER_ACTOR, cell.getId());
			}
	}

	@Override
	public void shutdown(Runnable onTermination, boolean await) {
		executorService.shutdownNow();
		
		if (onTermination!=null || await) {
			Thread waitOnTermination = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						executorService.awaitTermination(system.getConfig().awaitTerminationTimeout(), TimeUnit.MILLISECONDS);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						Thread.currentThread().interrupt();
//					}

					if (onTermination!=null)
						onTermination.run();
				}
			});
			
			waitOnTermination.start();
			if (await)
				try {
					waitOnTermination.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
		}
	}
	
	@Override
	public long getCount() {
		long sum = 0;
		for (ActorRunnableMetrics metrics : ClassicForkJoinWorkerThread.getAllMetrics())
			sum += metrics.counter.get();
		
		return sum;
	}
	
	@Override
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		for (ActorRunnableMetrics metrics : ClassicForkJoinWorkerThread.getAllMetrics())
			list.add(metrics.counter.get());
		return list;
	}
}
