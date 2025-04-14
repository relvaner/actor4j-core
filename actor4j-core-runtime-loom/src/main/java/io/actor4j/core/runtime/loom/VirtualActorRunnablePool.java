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
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorExecutionUnitPool;
import io.actor4j.core.runtime.ActorExecutionUnitPoolHandler;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;
import io.actor4j.core.runtime.utils.ProcessingTimeStatistics;

public class VirtualActorRunnablePool implements ActorExecutionUnitPool<VirtualActorRunnable> {
	protected final InternalActorRuntimeSystem system;
	
	protected final Map<UUID, Thread> virtualThreads;
	protected final VirtualActorRunnablePoolHandler virtualActorRunnablePoolHandler;
	
	protected final AtomicBoolean started;
	protected final AtomicReference<CountDownLatch> terminationCountDownLatch;

	protected final AtomicLong counter;
	
	protected final AtomicInteger processingTimeSampleCount;
	protected final Queue<Long> processingTimeSamples;
	
	protected final AtomicInteger cellsProcessingTimeSampleCount;

	public VirtualActorRunnablePool(InternalActorRuntimeSystem system, VirtualActorRunnablePoolHandlerFactory factory, boolean onlyResourceActors) {
		super();
		
		this.system = system;
		this.virtualActorRunnablePoolHandler = factory.apply(this);
		
		virtualThreads = new ConcurrentHashMap<>();
		started = new AtomicBoolean(false);
		terminationCountDownLatch = new AtomicReference<>();
		
		counter = new AtomicLong(0);
		
		processingTimeSampleCount = new AtomicInteger(0);
		processingTimeSamples = new ConcurrentLinkedQueue<>();
		
		cellsProcessingTimeSampleCount = new AtomicInteger(0);
		
		for (InternalActorCell cell : system.getCells().values())
			if (onlyResourceActors) {
				if (cell.getActor() instanceof ResourceActor)
					registerCell(cell);
			}
			else {
				if (!(cell.getActor() instanceof ResourceActor))
					registerCell(cell);
			}
		started.set(true);
		for (Thread t : virtualThreads.values())
			t.start();
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

	public void faultToleranceMethod(ActorMessage<?> message, InternalActorCell cell) {
		try {
			if (system.getConfig().processingTimeEnabled().get() || system.getConfig().trackProcessingTimePerActor().get()) {
				boolean statisticsEnabled = processingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
				boolean cellsStatisticsEnabled = cellsProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
				
				if (statisticsEnabled || cellsStatisticsEnabled) {
					long startTime = System.nanoTime();
					cell.internal_receive(message);
					long stopTime = System.nanoTime();

					if (statisticsEnabled && system.getConfig().processingTimeEnabled().get()) {
						processingTimeSamples.offer(stopTime-startTime);
						processingTimeSampleCount.incrementAndGet();
					}
					if (cellsStatisticsEnabled && system.getConfig().trackProcessingTimePerActor().get()) {
						cell.getProcessingTimeSamples().offer(stopTime-startTime);
						cellsProcessingTimeSampleCount.incrementAndGet();
					}
				}
				else
					cell.internal_receive(message);
			}
			else
				cell.internal_receive(message);
		}
		catch(Exception e) {
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.ACTOR, cell.getId());
			system.getStrategyOnFailure().handle(cell, e);
		}	
	}
	
	public void onTermination() {
		if (terminationCountDownLatch.get()!=null)
			terminationCountDownLatch.get().countDown();
	}
	
	public void registerCell(InternalActorCell cell) {
		VirtualActorRunnable virtualActorRunnable = virtualActorRunnablePoolHandler.registerCell(cell, this::faultToleranceMethod, this::onTermination, counter);
		Thread t = Thread.ofVirtual().name(actorLabel(cell.getActor())).unstarted(virtualActorRunnable);
		virtualThreads.put(virtualActorRunnable.idAsUUID(), t);
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

	public AtomicLong getCounter() {
		return counter;
	}

	@Override
	public long getCount() {
		return counter.longValue();
	}
	
	@Override
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		list.add(getCount());
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
		ProcessingTimeStatistics result = ProcessingTimeStatistics.of(processingTimeSamples, zScoreThreshold);
		processingTimeSampleCount.set(0);
		
		return List.of(result);
	}
	
	@Override
	public List<Double> getMeanProcessingTime() {
		double result = ProcessingTimeStatistics.meanProcessingTime(processingTimeSamples);
		processingTimeSampleCount.set(0);
		
		return List.of(result);
	}
	
	@Override
	public List<Double> getMedianProcessingTime() {
		double result = ProcessingTimeStatistics.medianProcessingTime(processingTimeSamples);
		processingTimeSamples.clear();
		processingTimeSampleCount.set(0);
		
		return List.of(result);
	}
	
	public Queue<Long> getProcessingTimeSamples() {
		return processingTimeSamples;
	}
	
	public AtomicInteger getProcessingTimeSampleCount() {
		return processingTimeSampleCount;
	}
	
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		return cellsProcessingTimeSampleCount;
	}
}
