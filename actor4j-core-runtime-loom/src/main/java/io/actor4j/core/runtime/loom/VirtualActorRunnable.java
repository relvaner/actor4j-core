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

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorExecutionUnit;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.fault.tolerance.FaultTolerance;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceMethod;

public abstract class VirtualActorRunnable implements Runnable, ActorExecutionUnit {
	protected final UUID faultToleranceId;
	
	protected final InternalActorSystem system;
	protected final InternalActorCell cell;

	protected final Runnable onTermination;
	protected final AtomicBoolean terminated;

	protected volatile Thread thread;
	
	public VirtualActorRunnable(InternalActorSystem system, InternalActorCell cell, Runnable onTermination) {
		super();

		this.system = system;
		this.cell = cell;
		this.onTermination = onTermination;

		faultToleranceId = UUID.randomUUID();
		terminated = new AtomicBoolean(false);
	}
	
	@Override
	public Object executionUnitId() {
		return cell.getId(); 
	}
	
	public ActorId id() {
		return cell.getId(); 
	}
	
	protected void faultToleranceMethod(ActorMessage<?> message) {
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
	
	public abstract void onRun();
	
	public abstract void newMessage(Thread t);
	
	protected VirtualActorRunnableMetrics getMetrics() {
		return ((InternalVirtualActorExecutorService)system.getExecutorService()).getVirtualActorRunnablePool().getMetrics();
	}
	
	public void metrics() {
		VirtualActorRunnableMetrics metrics = getMetrics();
		int totalProcessed = 0;
		
		if (system.getConfig().processingTimeEnabled().get() || system.getConfig().trackProcessingTimePerActor().get()) {
			boolean euPTEnabled = metrics.processingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			boolean cellsPTEnabled = metrics.cellsProcessingTimeSampleCount.get()<system.getConfig().maxProcessingTimeSamples();
			
			if (euPTEnabled || cellsPTEnabled) {
				long startTime = System.nanoTime();
				totalProcessed = processMessagesForMetrics();
				long stopTime = System.nanoTime();

				if (totalProcessed>0 && euPTEnabled && system.getConfig().processingTimeEnabled().get()) {
					metrics.processingTimeSamples.offer((stopTime-startTime) / totalProcessed);
					metrics.processingTimeSampleCount.incrementAndGet();
				}
				if (totalProcessed>0 && cellsPTEnabled && system.getConfig().trackProcessingTimePerActor().get()) {
					cell.getProcessingTimeSamples().offer((stopTime-startTime) / totalProcessed);
					metrics.cellsProcessingTimeSampleCount.incrementAndGet();
				}
			}
			else
				totalProcessed = processMessagesForMetrics();
		}
		else
			totalProcessed = processMessagesForMetrics();
		
		if (totalProcessed>0 && system.getConfig().trackRequestRatePerActor().get())
			cell.getRequestRate().add(totalProcessed);
	}
	
	public void processMessages() {
		processMessagesForMetrics();
	}
	
	public abstract int processMessagesForMetrics();
	
	@Override
	public void run() {
		thread = Thread.currentThread();
		
		FaultTolerance.runAndCatchThrowable(system.getExecutorService().getFaultToleranceManager(), new FaultToleranceMethod() {
			@Override
			public void run(Object faultToleranceId) {
				try {
					onRun();
				}
				finally {
					if (onTermination!=null)
						onTermination.run();
				}
			}
			
			@Override
			public void error(Throwable t) {
				t.printStackTrace();
			}
			
			@Override
			public void postRun() {
			}
		}, faultToleranceId);
	}
	
	public abstract Queue<ActorMessage<?>> directiveQueue();

	public abstract Queue<ActorMessage<?>> outerQueue();
	
	public void terminate() {
		terminated.set(true);
	}
}
