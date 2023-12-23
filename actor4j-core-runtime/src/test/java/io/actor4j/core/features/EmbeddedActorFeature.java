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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;

import static io.actor4j.core.logging.ActorLogger.*;
import static org.junit.Assert.*;

public class EmbeddedActorFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(1)
			.build();
		system = ActorSystem.create(AllFeaturesTest.factory(), config);
	}
	
	@Test(timeout=5000)
	public void test_become_unbecome() {
		final int SWAP=22;
		CountDownLatch testDone = new CountDownLatch(2);
		AtomicInteger counter = new AtomicInteger(0);
		
		UUID host = system.addActor(() -> new EmbeddedHostActor("host") {
			protected UUID client;
			@Override
			public void preStart() {
				client = addEmbeddedChild(() -> new EmbeddedActor("host:client") {
					@Override
					public boolean receive(ActorMessage<?> message) {
						boolean result = false;
						
						if (message.tag() == SWAP) {
							become(msg -> {
								logger().log(DEBUG, String.format(
										"Received String message: %s", msg.valueAsString()));
								unbecome(); 
								if (counter.incrementAndGet()==1)
									assertEquals("Hello World!", msg.valueAsString());
								else
									assertEquals("Hello World Again!", msg.valueAsString());
								testDone.countDown();
								return true;
							}, false);
							result = true;
						}
						
						return result;
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (!embedded(message, client))
					unhandled(message);
			}
		});
		
		system.send(ActorMessage.create(null, SWAP, system.SYSTEM_ID(), host));
		system.send(ActorMessage.create("Hello World!", 0, system.SYSTEM_ID(), host));
		system.send(ActorMessage.create(null, SWAP, system.SYSTEM_ID(), host));
		system.send(ActorMessage.create("Hello World Again!", 0, system.SYSTEM_ID(), host));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_await() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		AtomicBoolean[] postconditions = new AtomicBoolean[2];
		for (int i=0; i<postconditions.length; i++)
			postconditions[i] = new AtomicBoolean(false);

		UUID dest = system.addActor(() -> new EmbeddedHostActor("host") {
			protected UUID client;
			@Override
			public void preStart() {
				client = addEmbeddedChild(() -> new EmbeddedActor("host:client") {
					protected Predicate<ActorMessage<?>> action = (msg) -> {
						postconditions[0].set(true);
						unbecome();
						return true;
					};

					protected boolean first = true;
					
					@Override
					public boolean receive(ActorMessage<?> message) {
						if (first) {
							await(1, action);
							first = false;
						}
						else {
							postconditions[1].set(true);
							testDone.countDown();
						}
						
						return true;
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (!embedded(message, client))
					unhandled(message);
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
}
