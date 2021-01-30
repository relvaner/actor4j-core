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

import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.PrimaryActor;
import io.actor4j.core.actors.SecondaryActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ConcurrentActorGroup;

import static org.junit.Assert.*;

public class PrimarySecondaryActorFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem("PrimarySecondaryActorFeature");
	}
	
	@Test(timeout=5000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(system.getParallelismMin()*system.getParallelismFactor());
		
		AtomicBoolean primaryReceivedFromSystem = new AtomicBoolean(false);
		AtomicInteger secondaryReceived = new AtomicInteger(0);
		AtomicBoolean primaryReceived = new AtomicBoolean(false);
		
		ConcurrentActorGroup group = new ConcurrentActorGroup();
		UUID primary = system.addActor(() -> new PrimaryActor("primary", group, "instances", 
				(id) -> () -> new SecondaryActor(group, id) {
					@Override
					public void receive(ActorMessage<?> message) {
						if (message.source==primary) {
							secondaryReceived.incrementAndGet();
							testDone.countDown();
						}
						else if (message.source==system.SYSTEM_ID)
							publish(message);
					}
				}, system.getParallelismMin()*system.getParallelismFactor()-1) {
					@Override
					public void preStart() {
						super.preStart();
						
						group.addAll(hub.getPorts());
					}
			
					@Override
					public void receive(ActorMessage<?> message) {
						if (message.source==system.SYSTEM_ID) {
							primaryReceivedFromSystem.set(true);
							publish(message);
						}
						else if (message.tag==101) {
							primaryReceived.set(true);
							testDone.countDown();
						}
					}
		});
		
		system.start();
		
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, primary));
		system.send(new ActorMessage<>(null, 101, system.SYSTEM_ID, group.peek()));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(primaryReceivedFromSystem.get());
		assertEquals(system.getParallelismMin()*system.getParallelismFactor()-1, secondaryReceived.get());
		assertTrue(primaryReceived.get());
		
		system.shutdownWithActors(true);
	}
}
