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
package io.actor4j.core.features;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.internal.InternalActorSystem;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.AskPattern;

import static io.actor4j.core.actors.Actor.*;

public class WatchdogFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(2)
			.watchdogSyncTime(200)
			.watchdogTimeout(100)
			.build();
		system = ActorSystem.create(config);
	}
	
	@Test(timeout=5000)
	public void test_health_check() {
		UUID dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			} 
		});
		
		system.start();
		
		Optional<ActorMessage<?>> optional = AskPattern.ask(ActorMessage.create(null, HEALTH, null, system.SYSTEM_ID()), system);
		ActorMessage<?> message = optional.get();
		assertEquals(UP, message.tag());
		assertEquals(true, message.value()==null);
		
		optional = AskPattern.ask(ActorMessage.create(null, HEALTH, null, system.USER_ID()), system);
		message = optional.get();
		assertEquals(UP, message.tag());
		assertEquals(true, message.value()==null);
		
		optional = AskPattern.ask(ActorMessage.create(null, HEALTH, null, dest), system);
		message = optional.get();
		assertEquals(UP, message.tag());
		assertEquals(true, message.value()==null);
		
		system.shutdown(true);
	}

	@Test(timeout=5000)
	public void test_health_check_failed() {
		CountDownLatch testDone = new CountDownLatch(1);
		AtomicLong threadId = new AtomicLong();
		
		UUID dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				threadId.getAndSet(Thread.currentThread().getId());
				try {
					Thread.sleep(750);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				testDone.countDown();
			} 
		});
		
		system.start();
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(1, ((InternalActorSystem)system).getExecuterService().nonResponsiveThreadsCount());
		assertEquals(true, ((InternalActorSystem)system).getExecuterService().nonResponsiveThreads().contains(threadId.get()));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
	}
}
