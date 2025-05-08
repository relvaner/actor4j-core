/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.actor4j.core.runtime.AbstractActorExecutionUnitPool;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;
import io.actor4j.core.runtime.classic.utils.ClassicForkJoinWorkerThread;
import io.actor4j.core.runtime.utils.ProcessingTimeStatistics;

public class ActorRunnablePool extends AbstractActorExecutionUnitPool<ActorRunnable> {
	protected final ExecutorService executorService;

	protected final ActorRunnable actorRunnable;
	protected final ConcurrentHashMap<Long, ActorRunnableMetrics> metricsMap; // threadId -> ActorRunnableMetrics
	
	public ActorRunnablePool(InternalActorRuntimeSystem system) {
		super(system, null);
		
		metricsMap = new ConcurrentHashMap<>();
		
		int poolSize = system.getConfig().parallelism()*system.getConfig().parallelismFactor();
		executorService = new ForkJoinPool(poolSize,new ForkJoinWorkerThreadFactory() {
			@Override
			public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
				return new ClassicForkJoinWorkerThread(pool, metricsMap);
			}
			
		}, null, false);
		
		actorRunnable = new DefaultActorRunnable(system);
	}
	
	public ActorRunnableMetrics getMetrics() {
		return metricsMap.get(Thread.currentThread().threadId());
	}
	
	public List<ActorRunnableMetrics> getAllMetrics() {
		return metricsMap.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
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
		for (ActorRunnableMetrics metrics : getAllMetrics())
			sum += metrics.counter.get();
		
		return sum;
	}
	
	@Override
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		for (ActorRunnableMetrics metrics : getAllMetrics())
			list.add(metrics.counter.get());
		return list;
	}
	
	@Override
	public List<ProcessingTimeStatistics> getProcessingTimeStatistics() {
		return getProcessingTimeStatistics(-1);
	}
	
	@Override
	public List<ProcessingTimeStatistics> getProcessingTimeStatistics(double zScoreThreshold) {
		List<ProcessingTimeStatistics> list = new ArrayList<>();
		for (ActorRunnableMetrics metrics : getAllMetrics()) {
			ProcessingTimeStatistics result = ProcessingTimeStatistics.of(metrics.processingTimeSamples, zScoreThreshold);
			metrics.processingTimeSampleCount.set(0);
			list.add(result);
		}
		return list;
	}
	
	@Override
	public List<Double> getMeanProcessingTime() {
		List<Double> list = new ArrayList<>();
		for (ActorRunnableMetrics metrics : getAllMetrics()) {
			double result = ProcessingTimeStatistics.calculateMean(metrics.processingTimeSamples);
			metrics.processingTimeSampleCount.set(0);
			list.add(result);
		}
		return list;
	}
	
	@Override
	public List<Double> getMedianProcessingTime() {
		List<Double> list = new ArrayList<>();
		for (ActorRunnableMetrics metrics : getAllMetrics()) {
			double result = ProcessingTimeStatistics.calculateMedian(metrics.processingTimeSamples);
			metrics.processingTimeSamples.clear();
			metrics.processingTimeSampleCount.set(0);
			list.add(result);
		}
		return list;
	}
}
