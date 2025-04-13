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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.fault.tolerance.FaultTolerance;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceMethod;
import io.actor4j.core.runtime.ActorExecutionUnit;
import io.actor4j.core.runtime.ActorSystemError;

public abstract class ActorRunnable implements Runnable, ActorExecutionUnit {
	protected final long id;
	
	protected final UUID failsafeId;
	
	protected final InternalActorSystem system;
	
	protected final AtomicLong counter;
	protected final AtomicBoolean threadLoad;
	
	protected final AtomicInteger cellsStatisticValuesCounter;
	protected final AtomicBoolean cellsProcessingTimeEnabled;
	
	public ActorRunnable(InternalActorSystem system, long id) {
		super();
		
		this.system = system;
		this.id = id;
		failsafeId = UUID.randomUUID();

		threadLoad = new AtomicBoolean(false);
		counter = new AtomicLong(0);
		
		cellsStatisticValuesCounter = new AtomicInteger(0);
		cellsProcessingTimeEnabled = new AtomicBoolean(false);
	}

	@Override
	public Object executionUnitId() {
		return id;
	}
	
	protected void faultToleranceMethod(ActorMessage<?> message, InternalActorCell cell) {
		try {
			if (cellsProcessingTimeEnabled.get()) {
				boolean cellsStatisticsEnabled = cellsStatisticValuesCounter.get()<system.getConfig().maxStatisticValues();
				
				if (cellsStatisticsEnabled) {
					long startTime = System.nanoTime();
					cell.internal_receive(message);
					long stopTime = System.nanoTime();

					if (cellsStatisticsEnabled && cellsProcessingTimeEnabled.get()) {
						cell.getProcessingTimeStatistics().offer(stopTime-startTime);
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
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.ACTOR, cell.getId());
			system.getStrategyOnFailure().handle(cell, e);
		}	
	}
	
	public abstract void onRun(ClassicInternalActorCell cell);
	
	public void run(ClassicInternalActorCell cell, AtomicBoolean isScheduled, ActorMessageDispatcherCallback dispatcher) {
		FaultTolerance.runAndCatchThrowable(system.getExecutorService().getFaultToleranceManager(), new FaultToleranceMethod() {
			@Override
			public void run(UUID uuid) {
				onRun(cell);
				isScheduled.set(false);
		
				dispatcher.dispatchFromThread(cell, ActorRunnable.this);
			}
			
			@Override
			public void error(Throwable t) {
				t.printStackTrace();
			}
			
			@Override
			public void postRun() {
			}
		}, failsafeId);
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
		return threadLoad;
	}
	
	@Override
	public long getProcessingTimeStatistics() {
		return 0; // TODO
	}
	
	public AtomicInteger getCellsStatisticValuesCounter() {
		return cellsStatisticValuesCounter;
	}

	public AtomicBoolean getCellsProcessingTimeEnabled() {
		return cellsProcessingTimeEnabled;
	}
}
