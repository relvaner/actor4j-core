/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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

import java.util.List;
import java.util.UUID;

import io.actor4j.core.internal.failsafe.Method;
import io.actor4j.core.internal.failsafe.FailsafeMethod;

public abstract class WatchdogRunnable implements Runnable {
	protected final UUID uuid; // for failsafe
	
	protected final ActorSystemImpl system;
	protected final List<UUID> watchdogActors;

	public WatchdogRunnable(ActorSystemImpl system, List<UUID> watchdogActors) {
		super();
		
		this.system = system;
		this.watchdogActors = watchdogActors;
		uuid = UUID.randomUUID();
	}
	
	public abstract void onRun();
	
	@Override
	public void run() {
		FailsafeMethod.runAndCatchThrowable(system.executerService.failsafeManager, "watchdog", new Method() {
			@Override
			public void run(UUID uuid) {
				onRun();
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
	
	public UUID getUUID() {
		return uuid;
	}
}
