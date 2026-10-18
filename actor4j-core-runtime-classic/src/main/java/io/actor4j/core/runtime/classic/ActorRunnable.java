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

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.ActorExecutionUnit;
import io.actor4j.core.runtime.ActorSystemError;

public abstract class ActorRunnable implements Runnable, ActorExecutionUnit {
	protected final UUID faultToleranceId;
	
	protected final InternalActorSystem system;
	
	public ActorRunnable(InternalActorSystem system) {
		super();
		
		this.system = system;
		
		faultToleranceId = UUID.randomUUID();
	}

	@Deprecated
	@Override
	public Object executionUnitId() {
		// Not used!
		return null;
	}
	
	protected void faultToleranceMethod(ActorMessage<?> message, InternalActorCell cell) {
		try {
			cell.internal_receive(message);
		}
		catch(Exception e) {
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.ACTOR, cell.getId());
			system.getStrategyOnFailure().handle(cell, e);
		}
		catch(StackOverflowError | AssertionError e) { // non-fatal errors of the actor code -> supervision, VM errors (e.g. OutOfMemoryError) pass through
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.ACTOR, cell.getId());
			system.getStrategyOnFailure().handle(cell, new RuntimeException(e));
		}
	}
	
	public void onRun(ClassicInternalActorCell cell) {
		onRunForMetrics(cell);
	}
	
	public abstract int onRunForMetrics(ClassicInternalActorCell cell);
	
	protected ActorRunnableMetrics getMetrics() {
		return ((ClassicInternalActorExecutorService)system.getExecutorService()).getRunnablePool().getMetrics();
	}
	
	public void metrics(ClassicInternalActorCell cell) {
		ActorRunnableMetrics metrics = getMetrics();
		int totalProcessed = 0;
		
		if (system.getConfig().processingTimeEnabled().get() || system.getConfig().trackProcessingTimePerActor().get()) {
			boolean euPTEnabled = metrics.processingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			boolean cellsPTEnabled = metrics.cellsProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			
			if (euPTEnabled || cellsPTEnabled) {
				long startTime = System.nanoTime();
				totalProcessed = onRunForMetrics(cell);
				long stopTime = System.nanoTime();

				if (totalProcessed>0  && euPTEnabled && system.getConfig().processingTimeEnabled().get()) {
					metrics.processingTimeSamples.offer((stopTime-startTime) / totalProcessed);
					metrics.processingTimeSampleCount.incrementAndGet();
				}
				if (totalProcessed>0  && cellsPTEnabled && system.getConfig().trackProcessingTimePerActor().get()) {
					cell.getProcessingTimeSamples().offer((stopTime-startTime) / totalProcessed);
					metrics.cellsProcessingTimeSampleCount.incrementAndGet();
				}
			}
			else
				totalProcessed = onRunForMetrics(cell);
		}
		else
			totalProcessed = onRunForMetrics(cell);
		
		if (totalProcessed>0 && system.getConfig().trackRequestRatePerActor().get())
			cell.getRequestRate().add(totalProcessed);
	}
	
	public void run(ClassicInternalActorCell cell) {
		boolean error = false;
		Throwable throwable = null;
		
		try {
			if (system.getConfig().metricsEnabled().get())
				metrics(cell);
			else
				onRun(cell);
		}
		catch(Throwable t) {
			t.printStackTrace();
			
			error = true;
			throwable = t;
		}
		finally {
			cell.isScheduled().set(false);
			
			((ActorMessageDispatcherCallback)system.getMessageDispatcher()).dispatchFromThread(cell);
		}
		
		if (error)
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(throwable, null, null, faultToleranceId);
	}

	@Deprecated
	@Override
	public void run() {
		// not used
	}

	@Deprecated
	@Override
	public long getCount() {
		// Not used!
		return 0;
	}
	
	@Deprecated
	@Override
	public AtomicBoolean getLoad() {
		// Not used!
		return null;
	}
	
	@Deprecated
	@Override
	public Queue<Long> getProcessingTimeSamples() {
		// Not used!
		return null;
	}
	
	@Deprecated
	@Override
	public AtomicInteger getProcessingTimeSampleCount() {
		// Not used!
		return null;
	}
	
	@Deprecated
	@Override
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		// Not used!
		return null;
	}
}
