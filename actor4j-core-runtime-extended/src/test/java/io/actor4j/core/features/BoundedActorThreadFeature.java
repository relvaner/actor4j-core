/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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

import static io.actor4j.core.logging.ActorLogger.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.XActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorExecutionUnit;
import io.actor4j.core.runtime.InternalActorExecutorService;
import io.actor4j.core.runtime.extended.BoundedActorThread;
import io.actor4j.core.runtime.extended.XDefaultActorSystemImpl;

public class BoundedActorThreadFeature {
	protected XDefaultActorSystemImpl system;

	@BeforeClass
	public static void beforeClass() {
		systemLogger().setLevel(ERROR);
		logger().setLevel(ERROR);
	}

	@After
	public void after() {
		if (system!=null)
			system.shutdownWithActors(true);
	}

	protected XDefaultActorSystemImpl createSystem() {
		system = new XDefaultActorSystemImpl((XActorSystemConfig)XActorSystemConfig.builder()
			.unbounded(false)
			.parallelism(1)
			.parallelismFactor(1)
			.queueSize(1_000)
			.bufferQueueSize(100)
			.watchdogEnabled(false)
			.horizontalPodAutoscalerEnabled(false)
			.build());

		return system;
	}

	protected BoundedActorThread boundedActorThread() {
		ActorExecutionUnit unit = ((InternalActorExecutorService<?>)system.getExecutorService()).getExecutionUnitPool().getExecutionUnitList().get(0);
		assertTrue(unit instanceof BoundedActorThread);

		return (BoundedActorThread)unit;
	}

	protected static long waitUntilStable(LongSupplier value, long timeout) throws InterruptedException {
		long deadline = System.currentTimeMillis()+timeout;
		long last = -1;
		long current = value.getAsLong();
		while (current!=last && System.currentTimeMillis()<deadline) {
			last = current;
			Thread.sleep(200);
			current = value.getAsLong();
		}

		return current;
	}

	protected static void work(long nanos) {
		long start = System.nanoTime();
		while (System.nanoTime()-start<nanos)
			Thread.onSpinWait();
	}

	// flood from outside (outer queue, MpscArrayQueue): full queue drops the newest messages, every drop is counted
	@Test(timeout=30000)
	public void test_outer_queue_drops_are_counted() throws InterruptedException {
		AtomicLong received = new AtomicLong();

		createSystem();
		ActorId slow = system.addActor(() -> new Actor("slow") {
			@Override
			public void receive(ActorMessage<?> message) {
				work(20_000); // 20 µs
				received.incrementAndGet();
			}
		});
		system.start();
		BoundedActorThread thread = boundedActorThread();

		int sent = 200_000;
		for (int i=0; i<sent; i++)
			system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), slow));

		long receivedAfterFlood = waitUntilStable(received::get, 20000);
		assertTrue("messages dropped", thread.getDroppedMessages()>0);
		assertEquals("received + dropped == sent", sent, receivedAfterFlood+thread.getDroppedMessages());

		// queue has capacity again: no further drops
		long dropped = thread.getDroppedMessages();
		for (int i=0; i<500; i++)
			system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), slow));
		assertEquals(receivedAfterFlood+500, waitUntilStable(received::get, 10000));
		assertEquals(dropped, thread.getDroppedMessages());
	}

	// flood from an actor on the same thread (inner queue, BoundedArrayDeque): drops are counted as well
	@Test(timeout=30000)
	public void test_inner_queue_drops_are_counted() throws InterruptedException {
		AtomicLong received = new AtomicLong();
		int sent = 5_000; // more than queueSize within one receive

		createSystem();
		ActorId target = system.addActor(() -> new Actor("target") {
			@Override
			public void receive(ActorMessage<?> message) {
				received.incrementAndGet();
			}
		});
		ActorId sender = system.addActor(() -> new Actor("sender") {
			@Override
			public void receive(ActorMessage<?> message) {
				for (int i=0; i<sent; i++)
					tell(null, 1, target);
			}
		});
		system.start();
		BoundedActorThread thread = boundedActorThread();

		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), sender));

		long receivedAfterFlood = waitUntilStable(received::get, 10000);
		assertEquals("only queueSize messages fit", 1_000, receivedAfterFlood);
		assertEquals("received + dropped == sent", sent, receivedAfterFlood+thread.getDroppedMessages());
	}
}
