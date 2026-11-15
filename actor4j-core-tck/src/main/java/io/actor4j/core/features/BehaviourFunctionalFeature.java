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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.behaviour.ActorBehaviour;
import io.actor4j.core.actors.behaviour.ActorBehaviourContext;
import io.actor4j.core.actors.behaviour.BehaviourActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

// behaviours as return value (ActorBehaviour, ActorBehaviourContext, BehaviourActor)
public class BehaviourFunctionalFeature {
	public static final int SWITCH = 100;
	public static final int BACK = 101;
	public static final int PROBE = 102;
	public static final int QUIT = 103;	// not STOP: Actor.STOP is the internal stop directive
	public static final int FAIL = 104;
	public static final int REQUEST = 105;
	public static final int RESPONSE = 106;
	public static final int OTHER = 107;
	public static final int EMPTY = 108;
	public static final int IGNORE = 109;
	public static final int RESET = 110;

	protected ActorSystem system;

	@Before
	public void before() {
		system = ActorSystem.create(AllFeaturesTest.factory(), ActorSystemConfig.builder().parallelism(1).build());
	}

	// A <-> B, records the state of every PROBE, counts unhandled messages
	protected static class Switching extends BehaviourActor {
		protected final ActorBehaviour a = ActorBehaviour.named("A", this::a);
		protected final ActorBehaviour b = ActorBehaviour.named("B", this::b);

		protected final List<String> states;
		protected final AtomicInteger unhandled;
		protected final CountDownLatch done;

		public Switching(List<String> states, AtomicInteger unhandled, CountDownLatch done) {
			this.states = states;
			this.unhandled = unhandled;
			this.done = done;
		}

		@Override
		protected ActorBehaviour initial() {
			return a;
		}

		@Override
		public void unhandled(ActorMessage<?> message) {
			unhandled.incrementAndGet();
		}

		protected ActorBehaviour a(ActorMessage<?> message) {
			switch (message.tag()) {
				case SWITCH -> {
					return b;
				}
				case PROBE -> {
					return probe();
				}
				case EMPTY -> {
					return ActorBehaviour.empty();
				}
				case IGNORE -> {
					return ActorBehaviour.ignore();
				}
				default -> {
					return ActorBehaviour.unhandled();
				}
			}
		}

		protected ActorBehaviour b(ActorMessage<?> message) {
			switch (message.tag()) {
				case BACK -> {
					return a;
				}
				case RESET -> {
					return ActorBehaviour.initial();
				}
				case PROBE -> {
					return probe();
				}
				default -> {
					return ActorBehaviour.unhandled();
				}
			}
		}

		protected ActorBehaviour probe() {
			states.add(state());
			done.countDown();
			return ActorBehaviour.same();
		}
	}

	protected void send(ActorId dest, int... tags) {
		for (int tag : tags)
			system.send(ActorMessage.create(null, tag, system.SYSTEM_ID(), dest));
	}

	@Test(timeout=5000)
	public void test_switch_and_back() throws InterruptedException {
		List<String> states = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(3);
		ActorId actor = system.addActor(() -> new Switching(states, new AtomicInteger(), done));
		system.start();

		send(actor, PROBE, SWITCH, PROBE, BACK, PROBE);
		done.await();
		system.shutdownWithActors(true);

		assertEquals(List.of("A", "B", "A"), states);
	}

	@Test(timeout=5000)
	public void test_unhandled() throws InterruptedException {
		List<String> states = new CopyOnWriteArrayList<>();
		AtomicInteger unhandled = new AtomicInteger();
		CountDownLatch done = new CountDownLatch(1);
		ActorId actor = system.addActor(() -> new Switching(states, unhandled, done));
		system.start();

		send(actor, OTHER, BACK, PROBE); // BACK is unknown in A
		done.await();
		system.shutdownWithActors(true);

		assertEquals(2, unhandled.get());
		assertEquals(List.of("A"), states); // unhandled keeps the behaviour
	}

	@Test(timeout=5000)
	public void test_initial() throws InterruptedException {
		List<String> states = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(2);
		ActorId actor = system.addActor(() -> new Switching(states, new AtomicInteger(), done));
		system.start();

		send(actor, SWITCH, PROBE, RESET, PROBE);
		done.await();
		system.shutdownWithActors(true);

		assertEquals(List.of("B", "A"), states);
	}

	@Test(timeout=5000)
	public void test_empty_and_ignore() throws InterruptedException {
		AtomicInteger unhandledEmpty = new AtomicInteger();
		AtomicInteger unhandledIgnore = new AtomicInteger();
		Switching[] actors = new Switching[2];
		ActorId empty = system.addActor(() -> actors[0] = new Switching(new CopyOnWriteArrayList<>(), unhandledEmpty, new CountDownLatch(1)));
		ActorId ignore = system.addActor(() -> actors[1] = new Switching(new CopyOnWriteArrayList<>(), unhandledIgnore, new CountDownLatch(1)));
		system.start();

		send(empty, EMPTY, PROBE, SWITCH, OTHER);
		send(ignore, IGNORE, PROBE, SWITCH, OTHER);
		while (unhandledEmpty.get()<3) // empty: the three messages after EMPTY are unhandled
			Thread.sleep(10);
		Thread.sleep(200); // ignore: nothing to observe, give it time
		system.shutdownWithActors(true);
		
		assertEquals(3, unhandledEmpty.get());	// every message after EMPTY is unhandled
		assertEquals(0, unhandledIgnore.get());	// every message after IGNORE is dropped
		assertEquals("EMPTY", actors[0].state());
		assertEquals("IGNORE", actors[1].state());
	}

	@Test(timeout=5000)
	public void test_stopped() throws InterruptedException {
		CountDownLatch stopped = new CountDownLatch(1);
		ActorId actor = system.addActor(() -> new BehaviourActor() {
			@Override
			protected ActorBehaviour initial() {
				return (message) -> message.tag()==QUIT ? ActorBehaviour.stopped() : ActorBehaviour.same();
			}

			@Override
			public void postStop() {
				stopped.countDown();
			}
		});
		system.start();

		send(actor, QUIT);
		assertTrue(stopped.await(2, TimeUnit.SECONDS));
		system.shutdownWithActors(true);
	}

	@Test(timeout=5000)
	public void test_restart_starts_with_the_initial_behaviour() throws InterruptedException {
		List<String> states = new CopyOnWriteArrayList<>();
		AtomicInteger instances = new AtomicInteger();
		CountDownLatch done = new CountDownLatch(2);
		ActorId actor = system.addActor(() -> {
			instances.incrementAndGet();
			return new Switching(states, new AtomicInteger(), done) {
				@Override
				protected ActorBehaviour b(ActorMessage<?> message) {
					if (message.tag()==FAIL)
						throw new RuntimeException("expected: restart");
					return super.b(message);
				}
			};
		});
		system.start();

		send(actor, SWITCH, PROBE, FAIL, PROBE); // B, then a new instance starts with A
		done.await();
		system.shutdownWithActors(true);

		assertEquals(List.of("B", "A"), states);
		assertEquals(2, instances.get());
	}

	// client: IDLE -> request -> WAITING -> response/timeout -> IDLE
	protected static class Client extends BehaviourActor {
		protected final ActorBehaviour idle = ActorBehaviour.named("IDLE", this::idle);

		protected final List<String> events;
		protected final CountDownLatch done;
		protected final long timeout;

		public Client(List<String> events, CountDownLatch done, long timeout) {
			this.events = events;
			this.done = done;
			this.timeout = timeout;
		}

		@Override
		protected ActorBehaviour initial() {
			return idle;
		}

		@Override
		public void postStop() {
			events.add("stopped");
			done.countDown();
		}

		protected ActorBehaviour idle(ActorMessage<?> message) {
			switch (message.tag()) {
				case REQUEST -> {
					return ActorBehaviour.named("WAITING", await(RESPONSE, (response) -> {
						events.add("response in "+state());
						done.countDown();
						return idle;
					}, timeout, TimeUnit.MILLISECONDS, () -> {
						events.add("timeout in "+state());
						done.countDown();
						return ActorBehaviour.initial();
					}));
				}
				case OTHER -> {
					events.add("other in "+state());
					return ActorBehaviour.same();
				}
				case PROBE -> {
					events.add("probe in "+state());
					done.countDown();
					return ActorBehaviour.same();
				}
				case QUIT -> {
					return ActorBehaviour.stopped();
				}
				default -> {
					return ActorBehaviour.unhandled();
				}
			}
		}
	}

	@Test(timeout=5000)
	public void test_await_handles_other_messages() throws InterruptedException {
		List<String> events = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(2);
		ActorId client = system.addActor(() -> new Client(events, done, 2000));
		system.start();

		send(client, REQUEST, OTHER, RESPONSE, PROBE); // OTHER is handled while waiting
		done.await();
		List<String> result = List.copyOf(events); // before the shutdown (postStop adds "stopped")
		system.shutdownWithActors(true);

		assertEquals(List.of("other in WAITING", "response in WAITING", "probe in IDLE"), result);
	}

	@Test(timeout=5000)
	public void test_await_timeout() throws InterruptedException {
		List<String> events = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(1);
		ActorId client = system.addActor(() -> new Client(events, done, 100));
		system.start();

		send(client, REQUEST);
		done.await();
		send(client, PROBE); // after the timeout the client is IDLE again (ActorBehaviour.initial())
		while (events.size()<2)
			Thread.sleep(10);
		List<String> result = List.copyOf(events); // before the shutdown (postStop adds "stopped")
		system.shutdownWithActors(true);

		assertEquals(List.of("timeout in WAITING", "probe in IDLE"), result);
	}
	
	@Test(timeout=5000)
	public void test_await_stopped_while_waiting() throws InterruptedException {
		List<String> events = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(1);
		ActorId client = system.addActor(() -> new Client(events, done, 300));
		system.start();

		send(client, REQUEST, QUIT); // QUIT goes to idle, which stops: waiting ends
		done.await();
		Thread.sleep(500); // longer than the timeout
		system.shutdownWithActors(true);

		assertEquals(List.of("stopped"), events); // no timeout after the stop
	}

	@Test(timeout=5000)
	public void test_terminated_from_child() throws InterruptedException {
		CountDownLatch terminated = new CountDownLatch(1);
		ActorId parent = system.addActor(() -> new BehaviourActor() {
			protected ActorId child;

			@Override
			public void preStart() {
				child = addChild(() -> new BehaviourActor() {
					@Override
					protected ActorBehaviour initial() {
						return (message) -> message.tag()==QUIT ? ActorBehaviour.stopped() : ActorBehaviour.unhandled();
					}
				});
			}

			@Override
			protected ActorBehaviour initial() {
				return (message) -> {
					if (message.tag()==QUIT) {
						tell(null, QUIT, child);
						return ActorBehaviour.same();
					}
					if (message.tag()==TERMINATED && message.source()==child) { // child stopped by itself
						terminated.countDown();
						return ActorBehaviour.same();
					}
					return ActorBehaviour.unhandled();
				};
			}
		});
		system.start();

		send(parent, QUIT);
		assertTrue(terminated.await(2, TimeUnit.SECONDS));
		system.shutdownWithActors(true);
	}

	@Test(timeout=5000)
	public void test_composition_with_context() throws InterruptedException {
		List<String> states = new CopyOnWriteArrayList<>();
		CountDownLatch done = new CountDownLatch(2);
		ActorId actor = system.addActor(() -> new Actor() {	// own base class, no BehaviourActor
			protected final ActorBehaviour off = ActorBehaviour.named("OFF", this::off);
			protected final ActorBehaviour on = ActorBehaviour.named("ON", this::on);
			protected final ActorBehaviourContext behaviours = new ActorBehaviourContext(this, () -> off);

			@Override
			public void receive(ActorMessage<?> message) {
				behaviours.receive(message);
			}

			protected ActorBehaviour off(ActorMessage<?> message) {
				if (message.tag()==PROBE) {
					states.add(behaviours.state());
					done.countDown();
					return ActorBehaviour.same();
				}
				return message.tag()==SWITCH ? on : ActorBehaviour.unhandled();
			}

			protected ActorBehaviour on(ActorMessage<?> message) {
				if (message.tag()==PROBE) {
					states.add(behaviours.state());
					done.countDown();
					return ActorBehaviour.same();
				}
				return ActorBehaviour.unhandled();
			}
		});
		system.start();

		send(actor, PROBE, SWITCH, PROBE);
		done.await();
		system.shutdownWithActors(true);

		assertEquals(List.of("OFF", "ON"), states);
	}

	@Test
	public void test_become_not_supported() {
		BehaviourActor actor = new BehaviourActor() {
			@Override
			protected ActorBehaviour initial() {
				return ActorBehaviour.ignore();
			}
		};

		assertThrows(UnsupportedOperationException.class, () -> actor.become((message) -> {}));
		assertThrows(UnsupportedOperationException.class, () -> actor.unbecome());
	}

	@Test
	public void test_marker_is_not_a_behaviour() {
		assertThrows(UnsupportedOperationException.class, () -> ActorBehaviour.same().handle(null));
	}
}
