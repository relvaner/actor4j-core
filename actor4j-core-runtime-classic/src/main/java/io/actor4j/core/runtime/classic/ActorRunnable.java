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

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.ActorExecutionUnit;
import io.actor4j.core.runtime.ActorSystemError;

public abstract class ActorRunnable implements Runnable, ActorExecutionUnit {
	protected final long id;
	
	protected final UUID faultToleranceId;
	
	protected final InternalActorSystem system;
	
	protected final AtomicLong counter;
	protected final AtomicBoolean load;
	
	protected final AtomicInteger processingTimeSampleCount;
	protected final Queue<Long> processingTimeSamples;

	protected final AtomicInteger cellsProcessingTimeSampleCount;
	
	public ActorRunnable(InternalActorSystem system, long id) {
		super();
		
		this.system = system;
		this.id = id;
		faultToleranceId = UUID.randomUUID();

		load = new AtomicBoolean(false);
		counter = new AtomicLong(0);
		
		processingTimeSampleCount = new AtomicInteger(0);
		processingTimeSamples = new ConcurrentLinkedQueue<>();
		
		cellsProcessingTimeSampleCount = new AtomicInteger(0);
	}

	@Override
	public Object executionUnitId() {
		return id;
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
	
	public abstract void onRun(ClassicInternalActorCell cell);
	
	public void metrics(ClassicInternalActorCell cell) {
		if (system.getConfig().processingTimeEnabled().get() || system.getConfig().trackProcessingTimePerActor().get()) {
			boolean euPTEnabled = processingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			boolean cellsPTEnabled = cellsProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			
			if (euPTEnabled || cellsPTEnabled) {
				long startTime = System.nanoTime();
				onRun(cell);
				long stopTime = System.nanoTime();

				if (euPTEnabled && system.getConfig().processingTimeEnabled().get()) {
					processingTimeSamples.offer(stopTime-startTime);
					processingTimeSampleCount.incrementAndGet();
				}
				if (cellsPTEnabled && system.getConfig().trackProcessingTimePerActor().get()) {
					cell.getProcessingTimeSamples().offer(stopTime-startTime);
					cellsProcessingTimeSampleCount.incrementAndGet();
				}
			}
			else
				onRun(cell);
		}
		
		if (system.getConfig().trackRequestRatePerActor().get())
			cell.getRequestRate().incrementAndGet();
	}
	
	public void run(ClassicInternalActorCell cell, AtomicBoolean isScheduled, ActorMessageDispatcherCallback dispatcher) {
		boolean error = false;
		Throwable throwable = null;
		
		try {
			if (system.getConfig().metricsEnabled().get())
				metrics(cell);
			else
				onRun(cell);
			isScheduled.set(false);
	
			dispatcher.dispatchFromThread(cell, ActorRunnable.this);
		}
		catch(Throwable t) {
			t.printStackTrace();
			
			error = true;
			throwable = t;
		}
		finally {
			// empty
		}
		
		if (error)
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(throwable, null, null, faultToleranceId);
	}

	@Override
	public void run() {
		// not used
	}
	
	public AtomicLong getCounter() {
		return counter;
	}

	@Override
	public long getCount() {
		return counter.longValue();
	}
	
	@Override
	public AtomicBoolean getLoad() {
		// not used
		return load;
	}
	
	@Override
	public Queue<Long> getProcessingTimeSamples() {
		return processingTimeSamples;
	}
	
	@Override
	public AtomicInteger getProcessingTimeSampleCount() {
		return processingTimeSampleCount;
	}
	
	@Override
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		return cellsProcessingTimeSampleCount;
	}
}
