/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.runtime.fault.tolerance.FaultTolerance;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceMethod;

public abstract class WatchdogRunnable implements Runnable {
	protected final UUID faultToleranceId;
	
	protected final InternalActorSystem system;
	protected final List<ActorId> watchdogActors;

	public WatchdogRunnable(InternalActorSystem system, List<ActorId> watchdogActors) {
		super();
		
		this.system = system;
		this.watchdogActors = watchdogActors;
		faultToleranceId = UUID.randomUUID();
	}
	
	public abstract void onRun();
	
	@Override
	public void run() {
		FaultTolerance.runAndCatchThrowable(system.getExecutorService().getFaultToleranceManager(), ActorSystemError.WATCHDOG, new FaultToleranceMethod() {
			@Override
			public void run(Object faultToleranceId) {
				onRun();
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
	
	public UUID getFaultToleranceId() {
		return faultToleranceId;
	}
	
	public abstract boolean isResponsiveThread(int index);
	public abstract Set<Long> nonResponsiveThreads();
	public abstract int nonResponsiveThreadsCount();
}
