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

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

public class AwaitFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(1)
			.build();
		system = ActorSystem.create(config);
	}
		
	@Test(timeout=5000)
	public void test_await() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		AtomicBoolean[] postconditions = new AtomicBoolean[2];
		for (int i=0; i<postconditions.length; i++)
			postconditions[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected Consumer<ActorMessage<?>> action = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							postconditions[0].set(true);
							unbecome();
						}
					};
					
					protected boolean first = true;
					
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							await(1, action);
							first = false;
						}
						else {
							postconditions[1].set(true);
							testDone.countDown();
						}
					}
				};
			}
		});
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, postconditions[0].get());
		assertEquals(true, postconditions[1].get());
	}
	
	@Test(timeout=5000)
	public void test_await_timeout() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		AtomicBoolean[] postconditions = new AtomicBoolean[3];
		for (int i=0; i<postconditions.length; i++)
			postconditions[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected BiConsumer<ActorMessage<?>, Boolean> action = new BiConsumer<ActorMessage<?>, Boolean>() {
						@Override
						public void accept(ActorMessage<?> message, Boolean timeout) {
							postconditions[0].set(true);
							postconditions[2].set(timeout);
							unbecome();
						}
					};
					
					protected boolean first = true;
					
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							await((msg) -> msg.tag()==1, action, 500, TimeUnit.MILLISECONDS);
							first = false;
						}
						else {
							postconditions[1].set(true);
							testDone.countDown();
						}
					}
				};
			}
		});
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, postconditions[0].get());
		assertEquals(true, postconditions[1].get());
		assertEquals(false, postconditions[2].get());
	}
	
	@Test(timeout=5000)
	public void test_await_timeout_failed() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		AtomicBoolean[] postconditions = new AtomicBoolean[2];
		for (int i=0; i<postconditions.length; i++)
			postconditions[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected BiConsumer<ActorMessage<?>, Boolean> action = new BiConsumer<ActorMessage<?>, Boolean>() {
						@Override
						public void accept(ActorMessage<?> message, Boolean timeout) {
							postconditions[0].set(message==null);
							postconditions[1].set(timeout);
							unbecome();
							testDone.countDown();
						}
					};
					
					@Override
					public void receive(ActorMessage<?> message) {
						await((msg) -> msg.tag()==1, action, 500, TimeUnit.MILLISECONDS);
					}
				};
			}
		});
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, postconditions[0].get());
		assertEquals(true, postconditions[1].get());
	}
}
