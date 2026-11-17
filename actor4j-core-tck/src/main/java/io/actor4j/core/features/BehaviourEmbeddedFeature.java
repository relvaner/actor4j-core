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

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.EmbeddedHandlerActor;
import io.actor4j.core.actors.behaviour.ActorBehaviour;
import io.actor4j.core.actors.behaviour.BehaviourEmbeddedActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

// behaviours as return value in embedded actors (EmbeddedActorBehaviourContext, BehaviourEmbeddedActor)
public class BehaviourEmbeddedFeature {
	public static final int START = 100;
	public static final int STEP_1 = 101;
	public static final int STEP_2 = 102;
	public static final int SILENT = 103;	// the server does not answer
	public static final int PROBE = 104;

	protected ActorSystem system;

	@Before
	public void before() {
		system = ActorSystem.create(AllFeaturesTest.factory(), ActorSystemConfig.builder().parallelism(1).build());
	}

	// answers every message (except SILENT) to the source with the same tag and interaction
	protected static class Server extends Actor {
		@Override
		public void receive(ActorMessage<?> message) {
			if (message.tag()!=SILENT)
				tell(message.value(), message.tag(), message.source(), message.interaction());
		}
	}

	// one conversation per interaction: START -> STEP_1 -> STEP_2 -> done (stopped)
	protected static class Conversation extends BehaviourEmbeddedActor {
		protected final ActorId server;
		protected final List<String> events;
		protected final CountDownLatch done;
		protected final long timeout;

		public Conversation(ActorId server, List<String> events, CountDownLatch done, long timeout) {
			super();
			this.server = server;
			this.events = events;
			this.done = done;
			this.timeout = timeout;
		}

		@Override
		protected ActorBehaviour initial() {
			return ActorBehaviour.named("IDLE", (message) -> {
				if (message.tag()!=START)
					return ActorBehaviour.unhandled();

				int first = message.value() instanceof Integer tag ? tag : STEP_1;
				host().tell(message.interaction().toString(), first, server, message.interaction());
				return ActorBehaviour.named("STEP_1", behaviours.await(STEP_1, (response) -> {
					events.add("step 1 of "+response.value());
					host().tell(response.value(), STEP_2, server, response.interaction());
					return ActorBehaviour.named("STEP_2", behaviours.await(STEP_2, (response2) -> {
						events.add("step 2 of "+response2.value());
						done.countDown();
						return ActorBehaviour.stopped();
					}));
				}, timeout, TimeUnit.MILLISECONDS, () -> {
					events.add("timeout");
					done.countDown();
					return ActorBehaviour.stopped();
				}));
			});
		}
	}

	// routes by interaction to the conversations, PROBE answers the number of embedded actors
	protected static class Host extends EmbeddedHandlerActor {
		protected final ActorId server;
		protected final List<String> events;
		protected final CountDownLatch done;
		protected final long timeout;
		protected final List<Integer> embedded;

		public Host(ActorId server, List<String> events, CountDownLatch done, long timeout, List<Integer> embedded) {
			super();
			this.server = server;
			this.events = events;
			this.done = done;
			this.timeout = timeout;
			this.embedded = embedded;
		}

		@Override
		public void receive(ActorMessage<?> message) {
			if (message.tag()==PROBE)
				embedded.add(getHandlerRouter().size());
			else
				handle(message, () -> new Conversation(server, events, done, timeout));
		}
	}

	protected ActorId deploy(List<String> events, CountDownLatch done, long timeout, List<Integer> embedded) {
		ActorId server = system.addActor(() -> new Server());
		return system.addActor(() -> new Host(server, events, done, timeout, embedded));
	}

	protected void probe(ActorId host, List<Integer> embedded) throws InterruptedException {
		int size = embedded.size();
		system.send(ActorMessage.create(null, PROBE, system.SYSTEM_ID(), host));
		while (embedded.size()==size)
			Thread.sleep(10);
	}

	@Test(timeout=5000)
	public void test_conversations() throws InterruptedException {
		List<String> events = new CopyOnWriteArrayList<>();
		List<Integer> embedded = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(2);
		ActorId host = deploy(events, done, 2000, embedded);
		system.start();

		UUID a = UUID.randomUUID();
		UUID b = UUID.randomUUID();
		system.send(ActorMessage.create(null, START, system.SYSTEM_ID(), host, a));
		system.send(ActorMessage.create(null, START, system.SYSTEM_ID(), host, b));
		done.await();
		probe(host, embedded);
		system.shutdownWithActors(true);

		// two conversations at the same time, each with its own steps
		assertEquals(4, events.size());
		assertTrue(events.containsAll(List.of("step 1 of "+a, "step 2 of "+a, "step 1 of "+b, "step 2 of "+b)));
		assertTrue(events.indexOf("step 1 of "+a) < events.indexOf("step 2 of "+a));
		assertTrue(events.indexOf("step 1 of "+b) < events.indexOf("step 2 of "+b));
		// stopped() ends the embedded actors
		assertEquals(Integer.valueOf(0), embedded.get(0));
	}

	@Test(timeout=5000)
	public void test_timeout_over_the_host() throws InterruptedException {
		List<String> events = new CopyOnWriteArrayList<>();
		List<Integer> embedded = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(1);
		ActorId host = deploy(events, done, 100, embedded);
		system.start();

		system.send(ActorMessage.create(SILENT, START, system.SYSTEM_ID(), host, UUID.randomUUID()));
		probe(host, embedded); // waiting
		done.await();
		probe(host, embedded); // after the timeout
		system.shutdownWithActors(true);

		assertEquals(List.of("timeout"), events);
		assertEquals(List.of(1, 0), embedded);
	}

	@Test(timeout=5000)
	public void test_no_timeout_after_the_answer() throws InterruptedException {
		List<String> events = new CopyOnWriteArrayList<>();
		List<Integer> embedded = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(1);
		ActorId host = deploy(events, done, 200, embedded);
		system.start();

		system.send(ActorMessage.create(null, START, system.SYSTEM_ID(), host, UUID.randomUUID()));
		done.await();
		Thread.sleep(400); // longer than the timeout: a late timeout would start a new conversation
		probe(host, embedded);
		system.shutdownWithActors(true);

		assertEquals(2, events.size());
		assertFalse(events.contains("timeout"));
		assertEquals(Integer.valueOf(0), embedded.get(0));
	}

	@Test
	public void test_become_not_supported() {
		Conversation conversation = new Conversation(null, null, null, 0);
		assertThrows(UnsupportedOperationException.class, () -> conversation.become((message) -> true));
		assertThrows(UnsupportedOperationException.class, () -> conversation.await(START, (message) -> true));
	}
}
