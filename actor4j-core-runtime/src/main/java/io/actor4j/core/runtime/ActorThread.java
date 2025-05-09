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
package io.actor4j.core.runtime;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.logging.ActorLogger.systemLogger;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.fault.tolerance.FaultTolerance;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceMethod;

public abstract class ActorThread extends Thread implements ActorExecutionUnit {
	protected final UUID faultToleranceId;
	
	protected final InternalActorSystem system;
	
	protected Runnable onTermination;
	
	protected final AtomicLong counter;
	protected final AtomicBoolean threadLoad;
	
	protected final AtomicInteger threadProcessingTimeSampleCount;
	protected final Queue<Long> threadProcessingTimeSamples;
	
	protected final AtomicInteger cellsProcessingTimeSampleCount;
	
	public ActorThread(ThreadGroup group, String name, InternalActorSystem system) {
		super(group, name);
		
		this.system = system;
		faultToleranceId = UUID.randomUUID();
		
		threadLoad = new AtomicBoolean(false);
		counter = new AtomicLong(0);
		
		threadProcessingTimeSampleCount = new AtomicInteger(0);
		threadProcessingTimeSamples = new ConcurrentLinkedQueue<>();
		
		cellsProcessingTimeSampleCount = new AtomicInteger(0);
	}
	
	@Override
	public Object executionUnitId() {
		return threadId();
	}
	
	protected void faultToleranceMethod(ActorMessage<?> message, InternalActorCell cell) {
		try {
			cell.internal_receive(message);
		}
		catch(Exception e) {
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.ACTOR, cell.getId());
			system.getStrategyOnFailure().handle(cell, e);
		}	
	}
	
	protected void metrics(ActorMessage<?> message, InternalActorCell cell) {
		if (system.getConfig().processingTimeEnabled().get() || system.getConfig().trackProcessingTimePerActor().get()) {
			boolean threadPTEnabled = threadProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			boolean cellsPTEnabled = cellsProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			
			if (threadPTEnabled || cellsPTEnabled) {
				long startTime = System.nanoTime();
				faultToleranceMethod(message, cell);
				long stopTime = System.nanoTime();
				
				if (threadPTEnabled && system.getConfig().processingTimeEnabled().get()) {
					threadProcessingTimeSamples.offer(stopTime-startTime);
					threadProcessingTimeSampleCount.incrementAndGet();
				}
				if (cellsPTEnabled && system.getConfig().trackProcessingTimePerActor().get()) {
					cell.getProcessingTimeSamples().offer(stopTime-startTime);
					cellsProcessingTimeSampleCount.incrementAndGet();
				}
			}
			else
				faultToleranceMethod(message, cell);
		}
		
		if (system.getConfig().trackRequestRatePerActor().get())
			cell.getRequestRate().getAndIncrement();
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			InternalActorCell cell = system.getCells().get(message.dest());
			if (cell!=null) {
				if (system.getConfig().metricsEnabled().get())
					metrics(message, cell);
				else
					faultToleranceMethod(message, cell);
			}
			if (system.getConfig().counterEnabled().get())
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
		int retries = 0;
		final int maxRetries = system.getConfig().maxRetries(); // TODO
		
		while (!isInterrupted() && retries<maxRetries) { // stays operational in case of an error up to maxRetries
			final int retries_ = retries;
			FaultTolerance.runAndCatchThrowable(system.getExecutorService().getFaultToleranceManager(), new FaultToleranceMethod() {
				@Override
				public void run(Object faultToleranceId) {
					onRun();
					
					if (onTermination!=null)
						onTermination.run();
				}
				
				@Override
				public void error(Throwable t) {
					t.printStackTrace();
					
					if (retries_<maxRetries-1)
						systemLogger().log(WARN, String.format("[FT] Thread will be continued"));
					else
						systemLogger().log(ERROR, String.format("[FT] Thread is aborted"));
					
					// define optional fallback
				}
				
				@Override
				public void postRun() {
					// empty
				}
			}, faultToleranceId);
			
			retries++;
		}
	}
	
	public AtomicLong getCounter() {
		return counter;
	}

	public long getCount() {
		return counter.longValue();
	}
	
	@Override
	public AtomicBoolean getLoad() {
		return threadLoad;
	}
	
	@Override
	public Queue<Long> getProcessingTimeSamples() {
		return threadProcessingTimeSamples;
	}
	
	@Override
	public AtomicInteger getProcessingTimeSampleCount() {
		return threadProcessingTimeSampleCount;
	}
	
	@Override
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		return cellsProcessingTimeSampleCount;
	}

	public abstract Queue<ActorMessage<?>> getDirectiveQueue();
	
	public abstract Queue<ActorMessage<?>> getPriorityQueue();
	
	public abstract Queue<ActorMessage<?>> getServerQueue();
	
	public abstract Queue<ActorMessage<?>> getInnerQueue();
	
	public abstract Queue<ActorMessage<?>> getOuterQueue();
}
