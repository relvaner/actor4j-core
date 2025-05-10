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

import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.runtime.ActorExecutionUnitPool;
import io.actor4j.core.runtime.ActorExecutionUnitPoolHandler;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;
import io.actor4j.core.runtime.utils.ProcessingTimeStatistics;

public class VirtualActorRunnablePool implements ActorExecutionUnitPool<VirtualActorRunnable> {
	protected final InternalActorRuntimeSystem system;
	
	protected final List<VirtualActorRunnableMetrics> metricsList = new ArrayList<>();
	
	protected final Map<ActorId, Thread> virtualThreads;
	protected final VirtualActorRunnablePoolHandler virtualActorRunnablePoolHandler;
	
	protected final AtomicBoolean started;
	protected final AtomicReference<CountDownLatch> terminationCountDownLatch;
	
	protected final int parallelism;

	public VirtualActorRunnablePool(InternalActorRuntimeSystem system, VirtualActorRunnablePoolHandlerFactory factory, boolean onlyResourceActors) {
		super();
		
		this.system = system;
		this.virtualActorRunnablePoolHandler = factory.apply(this);
		
		virtualThreads = new ConcurrentHashMap<>();
		started = new AtomicBoolean(false);
		terminationCountDownLatch = new AtomicReference<>();
		
		parallelism = (Runtime.getRuntime().availableProcessors()+1) & ~1; // ensures parallelism is a power of 2
		for (int i=0; i<parallelism; i++)
			metricsList.add(new VirtualActorRunnableMetrics());
		
		Function<InternalActorCell, Boolean> registerCells = (cell) -> {
			if (onlyResourceActors) {
				if (cell.getActor() instanceof ResourceActor)
					registerCell(cell);
			}
			else {
				if (!(cell.getActor() instanceof ResourceActor))
					registerCell(cell);
			}
			
			return false;
		};
		system.internal_iterateCell((InternalActorCell)system.SYSTEM_ID(), registerCells);
		system.internal_iterateCell((InternalActorCell)system.USER_ID(), registerCells);
		started.set(true);
		for (Thread t : virtualThreads.values())
			t.start();
	}
	
	public VirtualActorRunnableMetrics getMetrics() {
//		int index = ThreadLocalRandom.current().nextInt(metricsList.size());
//		int index = (int) (Thread.currentThread().threadId() % parallelism);
		int index = (int) (Thread.currentThread().threadId() & (parallelism-1)); // parallelism must be a power of 2
		return metricsList.get(index);
	}
	
	@Override
	public List<VirtualActorRunnable> getExecutionUnitList() {
		return virtualActorRunnablePoolHandler.getVirtualActorRunnables().values().stream().collect(Collectors.toList());
	}
	
	@Override
	public ActorExecutionUnitPoolHandler<VirtualActorRunnable> getExecutionUnitPoolHandler() {
		return virtualActorRunnablePoolHandler;
	}
	
	public boolean isStarted() {
		return started.get();
	}

	@Override
	public void shutdown(Runnable onTermination, boolean await) {
		if (virtualThreads.size()>0) {
			terminationCountDownLatch.set(new CountDownLatch(virtualThreads.size()));
			
			for (Thread t : virtualThreads.values())
				t.interrupt();
			
			if (onTermination!=null || await) {
				Thread waitOnTermination = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							terminationCountDownLatch.get().await();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							Thread.currentThread().interrupt();
//						}

						if (onTermination!=null)
							onTermination.run();
						
						started.set(false);
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
			else
				started.set(false);
		}
		else {
			if (onTermination!=null)
				onTermination.run();
			
			started.set(false);	
		}
	}

	public VirtualActorRunnablePoolHandler getVirtualActorRunnablePoolHandler() {
		return virtualActorRunnablePoolHandler;
	}

	public void onTermination() {
		if (terminationCountDownLatch.get()!=null)
			terminationCountDownLatch.get().countDown();
	}
	
	public void registerCell(InternalActorCell cell) {
		VirtualActorRunnable virtualActorRunnable = virtualActorRunnablePoolHandler.registerCell(cell, this::onTermination);
		Thread t = Thread.ofVirtual().name(actorLabel(cell.getActor())).unstarted(virtualActorRunnable);
		virtualThreads.put(virtualActorRunnable.id(), t);
		if (started.get())
			t.start();
	}
	
	public void unregisterCell(InternalActorCell cell) {
		VirtualActorRunnable virtualActorRunnable = virtualActorRunnablePoolHandler.getVirtualActorRunnables().get(cell.getId());
		assert virtualActorRunnable!=null : "The runnable should be terminated only one times!";
		virtualActorRunnable.terminate();	
		virtualActorRunnablePoolHandler.unregisterCell(cell);
		virtualThreads.remove(cell.getId());	
	}
	
	public boolean isRegisteredCell(InternalActorCell cell) {
		return virtualActorRunnablePoolHandler.isRegisteredCell(cell);
	}
	
	@Override
	public long getCount() {
		long sum = 0;
		for (VirtualActorRunnableMetrics metrics : metricsList)
			sum += metrics.counter.get();
		
		return sum;
	}
	
	@Override
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		for (VirtualActorRunnableMetrics metrics : metricsList)
			list.add(metrics.counter.get());
		return list;
	}

	@Override
	public List<Boolean> getExecutionUnitLoads() {
		return List.of();
	}
	
	@Override
	public List<ProcessingTimeStatistics> getProcessingTimeStatistics() {
		return getProcessingTimeStatistics(-1);
	}
	
	@Override
	public List<ProcessingTimeStatistics> getProcessingTimeStatistics(double zScoreThreshold) {
		List<ProcessingTimeStatistics> list = new ArrayList<>();
		for (VirtualActorRunnableMetrics metrics : metricsList) {
			ProcessingTimeStatistics result = ProcessingTimeStatistics.of(metrics.processingTimeSamples, zScoreThreshold);
			metrics.processingTimeSampleCount.set(0);
			list.add(result);
		}
		return list;
	}
	
	@Override
	public List<Double> getMeanProcessingTime() {
		List<Double> list = new ArrayList<>();
		for (VirtualActorRunnableMetrics metrics : metricsList) {
			double result = ProcessingTimeStatistics.calculateMean(metrics.processingTimeSamples);
			metrics.processingTimeSampleCount.set(0);
			list.add(result);
		}
		return list;
	}
	
	@Override
	public List<Double> getMedianProcessingTime() {
		List<Double> list = new ArrayList<>();
		for (VirtualActorRunnableMetrics metrics : metricsList) {
			double result = ProcessingTimeStatistics.calculateMedian(metrics.processingTimeSamples);
			metrics.processingTimeSamples.clear();
			metrics.processingTimeSampleCount.set(0);
			list.add(result);
		}
		return list;
	}
}
