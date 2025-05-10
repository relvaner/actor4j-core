/*
 * Copyright (c) 2015-2023, David A. Bauer. All rights reserved.
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public class EmbeddedActorLifeCycleFeature {
protected ActorSystem system;
	
	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(1)
			.build();
		system = ActorSystem.create(AllFeaturesTest.factory(), config);
	}
	
	@Test(timeout=5000)
	public void test_stop() {
		CountDownLatch testDone = new CountDownLatch(3);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorId host = system.addActor(() -> new EmbeddedHostActor("host") {
			protected ActorId client;
			@Override
			public void preStart() {
				client = addEmbeddedChild(() -> new EmbeddedActor("host:client") {
					@Override
					public void postStop() {
						assertEquals(2, counter.incrementAndGet());
						testDone.countDown();
					}
					
					@Override
					public boolean receive(ActorMessage<?> message) {
						return true;
					}
				});
			}
			
			@Override
			public void postStop() {
				assertEquals(1, counter.incrementAndGet());
				testDone.countDown();
				super.postStop();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (!embedded(message, client))
					unhandled(message);
			}
		});
		
		system.addActor(() -> new Actor("observer") {
			@Override
			public void preStart() {
				watch(host);
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==TERMINATED && message.source().equals(host)) {
					assertEquals(3, counter.incrementAndGet());
					testDone.countDown();
				}
			}
		});
		
		system.send(ActorMessage.create(null, Actor.POISONPILL, system.SYSTEM_ID(), host));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_stop_kill() {
		CountDownLatch testDone = new CountDownLatch(3);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorId host = system.addActor(() -> new EmbeddedHostActor("host") {
			protected ActorId client;
			@Override
			public void preStart() {
				client = addEmbeddedChild(() -> new EmbeddedActor("host:client") {
					@Override
					public void postStop() {
						assertEquals(2, counter.incrementAndGet());
						testDone.countDown();
					}
					
					@Override
					public boolean receive(ActorMessage<?> message) {
						return true;
					}
				});
			}
			
			@Override
			public void postStop() {
				assertEquals(1, counter.incrementAndGet());
				testDone.countDown();
				super.postStop();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (!embedded(message, client))
					unhandled(message);
			}
		});
		
		system.addActor(() -> new Actor("observer") {
			@Override
			public void preStart() {
				watch(host);
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==TERMINATED && message.source().equals(host)) {
					assertEquals(3, counter.incrementAndGet());
					testDone.countDown();
				}
			}
		});
		
		system.send(ActorMessage.create(null, Actor.KILL, system.SYSTEM_ID(), host));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
