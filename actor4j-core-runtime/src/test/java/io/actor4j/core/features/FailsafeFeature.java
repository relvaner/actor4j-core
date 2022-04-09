/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package io.actor4j.core.features;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.internal.InternalActorSystem;
import io.actor4j.core.internal.failsafe.ErrorHandler;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

public class FailsafeFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(1)
			.build();
		system = ActorSystem.create(config);
	}
	
	@Test(timeout=5000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor("FailsafeFeatureActor") {
					@Override
					public void receive(ActorMessage<?> message) {
						throw new NullPointerException();
					}
					
					@Override
					public void preRestart(Exception reason) {
						super.preRestart(reason);
						assertEquals(new NullPointerException().getMessage(), reason.getMessage());
					}
					
					@Override
					public void postRestart(Exception reason) {
						super.postRestart(reason);
						assertEquals(new NullPointerException().getMessage(), reason.getMessage());
					}
				};
			}
		});
		
		ErrorHandler errorHandler = ((InternalActorSystem)system).getExecuterService().getFailsafeManager().getErrorHandler();
		((InternalActorSystem)system).getExecuterService().getFailsafeManager().setErrorHandler(new ErrorHandler() {
			@Override
			public void handle(Throwable t, String message, UUID uuid) {
				errorHandler.handle(t, message, uuid);
				assertEquals(new NullPointerException().getMessage(), t.getMessage());
				assertEquals("actor", message);
				assertEquals(dest, uuid);
				testDone.countDown();
			}
		});
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
	}
	
	@Test(timeout=5000)
	public void test_n_times() {
		final int maxRetries = system.getConfig().maxRetries();
		
		AtomicInteger counterPreRestart = new AtomicInteger(0);
		AtomicInteger counterPostStop = new AtomicInteger(0);
		
		CountDownLatch testDone = new CountDownLatch(maxRetries+1+1);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor("FailsafeFeatureActor") {
					@Override
					public void receive(ActorMessage<?> message) {
						throw new NullPointerException();
					}
					
					@Override
					public void preRestart(Exception reason) {
						super.preRestart(reason);
						assertEquals(new NullPointerException().getMessage(), reason.getMessage());
						counterPreRestart.incrementAndGet();
					}
					
					@Override
					public void postRestart(Exception reason) {
						super.postRestart(reason);
						assertEquals(new NullPointerException().getMessage(), reason.getMessage());
					}

					@Override
					public void postStop() {
						super.postStop();
						counterPostStop.incrementAndGet();
						if (counterPostStop.get()==maxRetries+1)
							testDone.countDown();
					}
				};
			}
		});
		
		ErrorHandler errorHandler = ((InternalActorSystem)system).getExecuterService().getFailsafeManager().getErrorHandler();
		((InternalActorSystem)system).getExecuterService().getFailsafeManager().setErrorHandler(new ErrorHandler() {
			@Override
			public void handle(Throwable t, String message, UUID uuid) {
				errorHandler.handle(t, message, uuid);
				assertEquals(new NullPointerException().getMessage(), t.getMessage());
				assertEquals("actor", message);
				assertEquals(dest, uuid);
				testDone.countDown(); // maxRetries+1
			}
		});
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest)); // -> restart
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest)); // -> restart
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest)); // -> restart
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest)); // -> stop
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		
		system.start();
		try {
			testDone.await();
			assertEquals(maxRetries, counterPreRestart.get());
			assertEquals(maxRetries+1, counterPostStop.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
	}
}
