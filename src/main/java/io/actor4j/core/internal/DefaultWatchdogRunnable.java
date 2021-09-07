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

import static io.actor4j.core.logging.ActorLogger.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.immutable.ImmutableList;

import static io.actor4j.core.internal.protocols.ActorProtocolTag.*;
import io.actor4j.core.messages.ActorMessage;

public class DefaultWatchdogRunnable extends WatchdogRunnable {
	protected final UUID mediator;
	
	public DefaultWatchdogRunnable(ActorSystemImpl system, List<UUID> watchdogActors) {
		super(system, watchdogActors);
		
		mediator = system.addSystemActor(() -> new ResourceActor("watchdog-controller") {
			List<CompletableFuture<Void>> futures;
			
			@SuppressWarnings("unchecked")
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.value!=null && message.value instanceof ImmutableList) {
					futures = ((ImmutableList<CompletableFuture<Void>>)message.value).get();
					for (UUID dest : watchdogActors)
						tell(null, INTERNAL_HEALTH_CHECK, dest);
				}
				else if (message.tag==UP && watchdogActors.contains(message.source))
					futures.get(watchdogActors.indexOf(message.source)).complete(null);
			}
		});
	}

	@Override
	public void onRun() {
		watchdog();
	}
	
	@SuppressWarnings("unchecked")
	public void watchdog() {
		systemLogger().log(DEBUG, String.format("[WATCHDOG] sync"));

		CompletableFuture<Void>[] futures = new CompletableFuture[watchdogActors.size()];
		for (int i=0; i<watchdogActors.size(); i++)
			futures[i] = new CompletableFuture<Void>();
		system.send(new ActorMessage<>(new ImmutableList<>(Arrays.asList(futures)), 0, system.SYSTEM_ID, mediator));
		
		try {
			CompletableFuture.allOf(futures).get(system.config.watchdogTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException e) {
			// e.printStackTrace();
		} catch (TimeoutException e) {
			systemLogger().log(WARN, String.format("[WATCHDOG] Responsiveness reduced!"));
			// e.printStackTrace();
		}

	}
}
