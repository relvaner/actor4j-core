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
	}
	
	public abstract void onRun(ClassicInternalActorCell cell);
	
	protected ActorRunnableMetrics getMetrics() {
		return ((ClassicInternalActorExecutorService)system.getExecutorService()).getRunnablePool().getMetrics();
	}
	
	public void metrics(ClassicInternalActorCell cell) {
		ActorRunnableMetrics metrics = getMetrics();
		
		if (system.getConfig().processingTimeEnabled().get() || system.getConfig().trackProcessingTimePerActor().get()) {
			boolean euPTEnabled = metrics.processingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			boolean cellsPTEnabled = metrics.cellsProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			
			if (euPTEnabled || cellsPTEnabled) {
				long startTime = System.nanoTime();
				onRun(cell);
				long stopTime = System.nanoTime();

				if (euPTEnabled && system.getConfig().processingTimeEnabled().get()) {
					metrics.processingTimeSamples.offer(stopTime-startTime);
					metrics.processingTimeSampleCount.incrementAndGet();
				}
				if (cellsPTEnabled && system.getConfig().trackProcessingTimePerActor().get()) {
					cell.getProcessingTimeSamples().offer(stopTime-startTime);
					metrics.cellsProcessingTimeSampleCount.incrementAndGet();
				}
			}
			else
				onRun(cell);
		}
		
		if (system.getConfig().trackRequestRatePerActor().get())
			cell.getRequestRate().incrementAndGet();
	}
	
	public void run(ClassicInternalActorCell cell) {
		boolean error = false;
		Throwable throwable = null;
		
		try {
			if (system.getConfig().metricsEnabled().get())
				metrics(cell);
			else
				onRun(cell);
			cell.isScheduled().set(false);
	
			((ActorMessageDispatcherCallback)system.getMessageDispatcher()).dispatchFromThread(cell);
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

	@Deprecated
	@Override
	public void run() {
		// not used
	}

	@Deprecated
	@Override
	public long getCount() {
		return 0;
	}
	
	@Deprecated
	@Override
	public AtomicBoolean getLoad() {
		return null;
	}
	
	@Deprecated
	@Override
	public Queue<Long> getProcessingTimeSamples() {
		return null;
	}
	
	@Deprecated
	@Override
	public AtomicInteger getProcessingTimeSampleCount() {
		return null;
	}
	
	@Deprecated
	@Override
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		return null;
	}
}
