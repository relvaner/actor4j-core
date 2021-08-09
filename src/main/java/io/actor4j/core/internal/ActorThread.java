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
package io.actor4j.core.internal;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.actor4j.core.internal.failsafe.FailsafeMethod;
import io.actor4j.core.internal.failsafe.Method;
import io.actor4j.core.messages.ActorMessage;

public abstract class ActorThread extends Thread {
	protected final UUID uuid; // for failsafe
	
	protected final ActorSystemImpl system;
	
	protected Runnable onTermination;
	
	protected final AtomicLong counter;
	protected final AtomicBoolean threadLoad;
	
	protected final AtomicInteger threadStatisticValuesCounter;
	protected final Queue<Long> threadProcessingTimeStatistics;
	
	protected final AtomicInteger cellsStatisticValuesCounter;
	protected final AtomicBoolean cellsProcessingTimeEnabled;
	
	public ActorThread(ThreadGroup group, String name, ActorSystemImpl system) {
		super(group, name);
		
		this.system = system;
		uuid = UUID.randomUUID();
		
		threadLoad = new AtomicBoolean(false);
		counter = new AtomicLong(0);
		
		threadStatisticValuesCounter = new AtomicInteger(0);
		threadProcessingTimeStatistics = new ConcurrentLinkedQueue<>();
		
		cellsStatisticValuesCounter = new AtomicInteger(0);
		cellsProcessingTimeEnabled = new AtomicBoolean(false);
	}
	
	protected void failsafeMethod(ActorMessage<?> message, ActorCell cell) {
		try {
			if (system.config.threadProcessingTimeEnabled.get() || cellsProcessingTimeEnabled.get()) {
				boolean threadStatisticsEnabled = threadStatisticValuesCounter.get()<system.config.maxStatisticValues;
				boolean cellsStatisticsEnabled = cellsStatisticValuesCounter.get()<system.config.maxStatisticValues;
				
				if (threadStatisticsEnabled || cellsStatisticsEnabled) {
					long startTime = System.nanoTime();
					cell.internal_receive(message);
					long stopTime = System.nanoTime();
					
					if (threadStatisticsEnabled && system.config.threadProcessingTimeEnabled.get()) {
						threadProcessingTimeStatistics.offer(stopTime-startTime);
						threadStatisticValuesCounter.incrementAndGet();
					}
					if (cellsStatisticsEnabled && cellsProcessingTimeEnabled.get()) {
						cell.processingTimeStatistics.offer(stopTime-startTime);
						cellsStatisticValuesCounter.incrementAndGet();
					}
				}
				else
					cell.internal_receive(message);
			}
			else
				cell.internal_receive(message);
		}
		catch(Exception e) {
			system.executerService.failsafeManager.notifyErrorHandler(e, "actor", cell.id);
			system.actorStrategyOnFailure.handle(cell, e);
		}	
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			ActorCell cell = system.cells.get(message.dest);
			if (cell!=null) {
				cell.requestRate.getAndIncrement();
				failsafeMethod(message, cell);
			}
			if (system.config.counterEnabled.get())
				counter.getAndIncrement();
			
			result = true;
		} 
		
		return result;
	}
	
	public abstract void directiveQueue(ActorMessage<?> message);
	
	public abstract void priorityQueue(ActorMessage<?> message);
	
	public abstract void serverQueue(ActorMessage<?> message);
	
	public abstract void outerQueue(ActorMessage<?> message);
	
	public abstract void innerQueue(ActorMessage<?> message);
	
	public abstract void onRun();
	
	protected abstract void newMessage();
		
	@Override
	public void run() {
		FailsafeMethod.runAndCatchThrowable(system.executerService.failsafeManager, new Method() {
			@Override
			public void run(UUID uuid) {
				onRun();
				
				if (onTermination!=null)
					onTermination.run();
			}
			
			@Override
			public void error(Throwable t) {
				t.printStackTrace();
			}
			
			@Override
			public void after() {
			}
		}, uuid);
	}
	
	public AtomicLong getCounter() {
		return counter;
	}

	public long getCount() {
		return counter.longValue();
	}
	
	public AtomicBoolean getThreadLoad() {
		return threadLoad;
	}
	
	public long getThreadProcessingTimeStatistics() {
		long sum = 0;
		int count = 0;
		for (Long value=null; (value=threadProcessingTimeStatistics.poll())!=null; count++) 
			sum += value;
		threadStatisticValuesCounter.set(0);
		
		return sum>0 ? sum/count : 0;
	}
	
	public AtomicInteger getCellsStatisticValuesCounter() {
		return cellsStatisticValuesCounter;
	}

	public AtomicBoolean getCellsProcessingTimeEnabled() {
		return cellsProcessingTimeEnabled;
	}

	public abstract Queue<ActorMessage<?>> getDirectiveQueue();
	
	public abstract Queue<ActorMessage<?>> getPriorityQueue();
	
	public abstract Queue<ActorMessage<?>> getServerQueue();
	
	public abstract Queue<ActorMessage<?>> getInnerQueue();
	
	public abstract Queue<ActorMessage<?>> getOuterQueue();
	
	public UUID getUUID() {
		return uuid;
	}
}
