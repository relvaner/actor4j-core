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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorExecutionUnit;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.fault.tolerance.FaultTolerance;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceMethod;

public abstract class VirtualActorRunnable implements Runnable, ActorExecutionUnit {
	protected final UUID failsafeId;
	
	protected final InternalActorSystem system;
	protected final InternalActorCell cell;
	
	protected final BiConsumer<ActorMessage<?>, InternalActorCell> faultToleranceMethod;
	protected final AtomicLong counter;
	protected final Runnable onTermination;
	protected final AtomicBoolean terminated;
	
	public VirtualActorRunnable(InternalActorSystem system, InternalActorCell cell, BiConsumer<ActorMessage<?>, InternalActorCell> faultToleranceMethod, Runnable onTermination, AtomicLong counter) {
		super();

		this.system = system;
		this.cell = cell;
		this.onTermination = onTermination;
		this.faultToleranceMethod = faultToleranceMethod;
		this.counter = counter;

		failsafeId = UUID.randomUUID();
		terminated = new AtomicBoolean(false);
	}
	
	@Override
	public Object executionUnitId() {
		return cell.getId(); 
	}
	
	public UUID idAsUUID() {
		return cell.getId(); 
	}
	
	public abstract void onRun();
	
	public abstract void newMessage(Thread t);
	
	@Override
	public void run() {
		FaultTolerance.runAndCatchThrowable(system.getExecutorService().getFaultToleranceManager(), new FaultToleranceMethod() {
			@Override
			public void run(UUID uuid) {
				onRun();
				
				onTermination.run();
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
	
	public abstract Queue<ActorMessage<?>> directiveQueue();

	public abstract Queue<ActorMessage<?>> outerQueue();
	
	public void terminate() {
		terminated.set(true);
	}
}
