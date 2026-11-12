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
package io.actor4j.core.features.regression;

import static io.actor4j.core.supervisor.SupervisorStrategyDirective.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;

import io.actor4j.core.ActorService;
import io.actor4j.core.ActorSystem;
import io.actor4j.core.features.AllFeaturesTest;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.actors.StatefulResourceActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.id.GlobalId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.RemotePodMessageDTO;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.config.ActorRuntimeConfig;
import io.actor4j.core.runtime.config.InternalServerCallback;
import io.actor4j.core.pods.functions.FunctionPod;
import io.actor4j.core.pods.functions.PodFunction;
import io.actor4j.core.pods.functions.PodRemoteFunction;
import io.actor4j.core.pods.functions.RemoteFunctionPod;
import io.actor4j.core.utils.Reply;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.service.discovery.ServiceDiscoveryActor;
import io.actor4j.core.supervisor.OneForOneSupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategyDirective;
import io.actor4j.core.utils.DeepCopyable;

/**
 * Regression tests for fixes in actor4j-core-runtime-base and the runtimes (review 2026-10).
 * AI-maintained.
 */
public class RegressionRuntimeFeature {
	protected ActorSystem system;

	protected ActorSystem createSystem(ActorSystemConfig.Builder<?> builder) {
		system = ActorSystem.create(AllFeaturesTest.factory(), builder.build());
		return system;
	}

	protected ActorSystem createSystem() {
		return createSystem(ActorSystemConfig.builder());
	}

	@After
	public void after() throws InterruptedException {
		if (system!=null) {
			// bounded: shutdownWithActors(true) waits without limit if an actor never stops,
			// and @Test(timeout) does not cover @After
			Thread shutdown = new Thread(() -> system.shutdownWithActors(true), "regression-shutdown");
			shutdown.setDaemon(true);
			shutdown.start();
			shutdown.join(5_000);
			if (shutdown.isAlive())
				system.shutdown(false);
		}
	}

	protected static void await(CountDownLatch latch) {
		try {
			assertTrue("timeout", latch.await(3, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			fail("interrupted");
		}
	}

	// runtime-base H1: tell() to an unknown alias must not crash the sender
	@Test(timeout=5000)
	public void test_tell_to_unknown_alias() {
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem();
		ActorId sender = system.addActor(() -> new Actor("sender") {
			@Override
			public void receive(ActorMessage<?> message) {
				tell(null, 1, "does-not-exist");
				testDone.countDown(); // only reached if tell() did not throw
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), sender));

		await(testDone);
	}

	// runtime-base H1: tell() to an unknown alias in preStart() must not break start()
	@Test(timeout=5000)
	public void test_tell_to_unknown_alias_in_preStart() {
		CountDownLatch onStartup = new CountDownLatch(1);
		CountDownLatch received = new CountDownLatch(1);

		createSystem();
		ActorId receiver = system.addActor(() -> new Actor("receiver") {
			@Override
			public void receive(ActorMessage<?> message) {
				received.countDown();
			}
		});
		system.addActor(() -> new Actor("sender") {
			@Override
			public void preStart() {
				tell(null, 1, "does-not-exist");
			}

			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), receiver)); // buffered before start
		system.start(() -> onStartup.countDown(), null);

		await(onStartup);
		await(received);
	}

	protected static final AtomicBoolean slowCopy = new AtomicBoolean();

	// value whose copy is deliberately slow, used to keep the buffer drain in start() busy
	protected static final class SlowCopy implements DeepCopyable<SlowCopy> {
		@Override
		public SlowCopy deepCopy() {
			if (slowCopy.get())
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			return this;
		}
	}

	// runtime-base M4: messages sent while the buffer is drained must not overtake buffered messages.
	// Buffer before start: [M -> x, 20 x SlowCopy -> z, A -> y (from x.preStart)]. Draining the slow
	// copies takes ~200 ms; meanwhile x processes M and sends B to y. B must arrive after A.
	@Test(timeout=10000)
	public void test_start_keeps_order_of_buffered_messages() {
		final int A = 1;
		final int B = 2;
		List<Integer> order = new CopyOnWriteArrayList<>();
		CountDownLatch testDone = new CountDownLatch(2);

		createSystem();
		ActorId y = system.addActor(() -> new Actor("y") {
			@Override
			public void receive(ActorMessage<?> message) {
				order.add(message.tag());
				testDone.countDown();
			}
		});
		ActorId z = system.addActor(() -> new Actor("z") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});
		ActorId x = system.addActor(() -> new Actor("x") {
			protected boolean sent;

			@Override
			public void preStart() {
				tell(null, A, y); // buffered behind the slow copies
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (!sent) {
					sent = true;
					tell(null, B, y); // sent while the buffer is still drained
				}
			}
		});

		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), x));
		for (int i=0; i<20; i++)
			system.send(ActorMessage.create(new SlowCopy(), 0, system.SYSTEM_ID(), z));

		slowCopy.set(true); // only the copies made while draining are slow
		try {
			system.start();
			await(testDone);
		}
		finally {
			slowCopy.set(false);
		}

		assertEquals(List.of(A, B), order);
	}

	// runtime-base H2: after a restart the new instance must receive the messages
	@Test(timeout=5000)
	public void test_restart_drops_behaviours_of_old_instance() {
		AtomicInteger instances = new AtomicInteger();
		List<Integer> receivedBy = new CopyOnWriteArrayList<>();
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("restart") {
			final int instance = instances.incrementAndGet();

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1)
					become(msg -> {
						if (msg.tag()==2)
							throw new RuntimeException("restart");
						receivedBy.add(instance);
						testDone.countDown();
					});
				else {
					receivedBy.add(instance);
					testDone.countDown();
				}
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest)); // become
		system.send(ActorMessage.create(null, 2, system.SYSTEM_ID(), dest)); // failure -> restart
		system.send(ActorMessage.create(null, 3, system.SYSTEM_ID(), dest));

		await(testDone);
		assertEquals(2, instances.get());
		assertEquals(List.of(2), receivedBy);
	}

	// runtime-base H3: unhandled() with a message without source
	@Test(timeout=5000)
	public void test_unhandled_without_source() {
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("unhandled") {
			@Override
			public void receive(ActorMessage<?> message) {
				unhandled(message);
				testDone.countDown(); // only reached if unhandled() did not throw
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, null, dest));

		await(testDone);
	}

	// runtime-base H4: a stateful resource actor must keep working after an exception
	@Test(timeout=5000)
	public void test_stateful_resource_actor_after_exception() {
		CountDownLatch testDone = new CountDownLatch(5);

		createSystem();
		ActorId dest = system.addActor(() -> new StatefulResourceActor("resource") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1)
					throw new RuntimeException("failure");
				testDone.countDown();
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));
		for (int i=0; i<5; i++)
			system.send(ActorMessage.create(null, 2, system.SYSTEM_ID(), dest));

		await(testDone);
	}

	// runtime-base H5: a stopped actor must not stay exposed
	@Test(timeout=5000)
	public void test_unexpose_on_stop() throws InterruptedException {
		AtomicReference<UUID> globalId = new AtomicReference<>();
		CountDownLatch exposed = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("exposed") {
			@Override
			public void preStart() {
				expose();
				globalId.set(self().globalId());
				exposed.countDown();
			}

			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});
		system.start();
		await(exposed);
		assertNotNull(system.getActor(globalId.get()));

		system.send(ActorMessage.create(null, Actor.STOP, system.SYSTEM_ID(), dest));
		for (int i=0; i<60 && system.getActor(globalId.get())!=null; i++)
			Thread.sleep(50);

		assertNull(system.getActor(globalId.get()));
	}

	// runtime H1: maxRetries(0) of the supervisor must not stop the worker threads
	@Test(timeout=5000)
	public void test_supervisor_max_retries_zero() {
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem(ActorSystemConfig.builder().maxRetries(0));
		ActorId dest = system.addActor(() -> new Actor("retries") {
			@Override
			public void receive(ActorMessage<?> message) {
				testDone.countDown();
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), dest));

		await(testDone);
	}

	// runtime-base M1: ESCALATE restarts the escalating parent (directive of the grandparent)
	@Test(timeout=5000)
	public void test_escalate_restarts_parent() {
		AtomicInteger parentInstances = new AtomicInteger();
		CountDownLatch parentRestarted = new CountDownLatch(1);

		createSystem();
		ActorId parent = system.addActor(() -> new Actor("parent") {
			final int instance = parentInstances.incrementAndGet();
			protected ActorId child;

			@Override
			public void preStart() {
				if (instance==2)
					parentRestarted.countDown();
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						throw new RuntimeException("escalate");
					}
				});
			}

			@Override
			public void receive(ActorMessage<?> message) {
				send(ActorMessage.create(null, 0, self(), child));
			}

			@Override
			public SupervisorStrategy supervisorStrategy() {
				return new OneForOneSupervisorStrategy(3, 1_000) {
					@Override
					public SupervisorStrategyDirective apply(Exception e) {
						return ESCALATE;
					}
				};
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));

		await(parentRestarted);
		assertEquals(2, parentInstances.get());
	}

	// SDK H8: UNPUBLISH_SERVICE with an unknown id must not crash the service discovery
	@Test(timeout=5000)
	public void test_service_discovery_unpublish_unknown_id() {
		AtomicInteger instances = new AtomicInteger();
		AtomicBoolean replied = new AtomicBoolean();
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem();
		ActorId discovery = system.addActor(() -> new ServiceDiscoveryActor("discovery") {
			{
				instances.incrementAndGet();
			}
		});
		ActorId client = system.addActor(() -> new Actor("client") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==ServiceDiscoveryActor.LOOKUP_SERVICE) {
					replied.set(true);
					testDone.countDown();
				}
				else {
					// same sender -> same receiver: UNPUBLISH is processed before LOOKUP
					tell(UUID.randomUUID(), ServiceDiscoveryActor.UNPUBLISH_SERVICE, discovery);
					tell("topic", ServiceDiscoveryActor.LOOKUP_SERVICE, discovery);
				}
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), client));

		await(testDone);
		assertTrue(replied.get());
		assertEquals(1, instances.get()); // no restart caused by the unknown id
	}

	// runtime-base M3: postRestart() calls preStart() again, the alias must not be registered twice
	@Test(timeout=5000)
	public void test_alias_after_restart_and_stop() throws InterruptedException {
		CountDownLatch restarted = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("alias-restart") {
			@Override
			public void preStart() {
				setAlias("regression-alias-restart");
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1)
					throw new RuntimeException("restart");
			}

			@Override
			public void postRestart(Exception reason) {
				super.postRestart(reason);
				restarted.countDown();
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));

		await(restarted);
		assertEquals(List.of(dest), system.getActorsFromAlias("regression-alias-restart"));

		system.send(ActorMessage.create(null, Actor.STOP, system.SYSTEM_ID(), dest));
		for (int i=0; i<60 && !system.getActorsFromAlias("regression-alias-restart").isEmpty(); i++)
			Thread.sleep(50);

		assertTrue(system.getActorsFromAlias("regression-alias-restart").isEmpty());
	}

	// runtime-base M3: a new alias replaces the old one, both are cleaned up on stop
	@Test(timeout=5000)
	public void test_alias_replaced_and_stop() throws InterruptedException {
		CountDownLatch aliased = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("alias-replaced") {
			@Override
			public void preStart() {
				setAlias("regression-alias-first");
				setAlias("regression-alias-second");
				aliased.countDown();
			}

			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});
		system.start();

		await(aliased);
		assertEquals("regression-alias-second", system.getAliasFromActor(dest));
		assertTrue(system.getActorsFromAlias("regression-alias-first").isEmpty());
		assertEquals(List.of(dest), system.getActorsFromAlias("regression-alias-second"));

		system.send(ActorMessage.create(null, Actor.STOP, system.SYSTEM_ID(), dest));
		for (int i=0; i<60 && !system.getActorsFromAlias("regression-alias-second").isEmpty(); i++)
			Thread.sleep(50);

		assertTrue(system.getActorsFromAlias("regression-alias-second").isEmpty());
	}

	// runtime-base M5: message is delivered as soon as the actor becomes active
	@Test(timeout=5000)
	public void test_send_when_active() throws InterruptedException {
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("send-when-active") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1)
					testDone.countDown();
			}
		});
		system.start();

		((InternalActorCell)dest).setActive(false);
		system.sendWhenActive(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest), 2, TimeUnit.SECONDS);
		Thread.sleep(100);
		assertEquals(1, testDone.getCount());

		((InternalActorCell)dest).setActive(true);
		await(testDone);
	}

	// runtime-base M5: the periodic task ends after the timeout, the message is dropped
	@Test(timeout=5000)
	public void test_send_when_active_timeout() throws InterruptedException {
		AtomicInteger received = new AtomicInteger();
		CountDownLatch control = new CountDownLatch(1);

		createSystem();
		ActorId dest = system.addActor(() -> new Actor("send-when-active-timeout") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1)
					received.incrementAndGet();
				else if (message.tag()==2)
					control.countDown();
			}
		});
		system.start();

		((InternalActorCell)dest).setActive(false);
		system.sendWhenActive(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest), 100, TimeUnit.MILLISECONDS);
		Thread.sleep(300);

		((InternalActorCell)dest).setActive(true);
		system.send(ActorMessage.create(null, 2, system.SYSTEM_ID(), dest));
		await(control);
		Thread.sleep(100);
		assertEquals(0, received.get());
	}

	// SDK FunctionPod/RemoteFunctionPod: tag 1 is answered later (pending) via a backend actor (tag 2),
	// the final reply must go to the original sender and not to the backend; tag 3 is answered directly
	protected static final List<Object> functionLog = new CopyOnWriteArrayList<>(); // [interaction, replyAllowed] per request

	protected static PodFunction createPendingFunction(ActorRef host, PodContext context) {
		return new PodRemoteFunction(host, context) {
			@Override
			public Reply handle(ActorMessage<?> message, UUID interaction, boolean replyAllowed) {
				Reply result = null;
				if (message.tag()==1 || message.tag()==3)
					functionLog.addAll(List.of(interaction, replyAllowed));

				if (message.tag()==1) {
					host.tell(null, 1, "regression-backend", interaction);
					result = Reply.pending();
				}
				else if (message.tag()==2)
					result = Reply.of(message.value(), 42, message.interaction());
				else if (message.tag()==3)
					result = Reply.of("direct", 42, interaction);

				return result;
			}

			@Override
			public Reply handle(RemotePodMessage remoteMessage, UUID interaction, boolean replyExpected) {
				return null;
			}
		};
	}

	public static class PendingFunctionPod extends FunctionPod {
		@Override
		public PodFunction createFunction(ActorRef host, PodContext context) {
			return createPendingFunction(host, context);
		}

		@Override
		public String domain() {
			return "regression-pending-function";
		}
	}

	public static class PendingRemoteFunctionPod extends RemoteFunctionPod {
		@Override
		public PodRemoteFunction createFunction(ActorRef host, PodContext context) {
			return (PodRemoteFunction)createPendingFunction(host, context);
		}

		@Override
		public String domain() {
			return "regression-pending-remote-function";
		}
	}

	protected void pendingReplyToOriginalSender(PodFactory factory, String domain, UUID interaction) {
		AtomicReference<Object> reply = new AtomicReference<>();
		AtomicReference<UUID> replyInteraction = new AtomicReference<>(UUID.randomUUID()); // must be overwritten
		AtomicBoolean backendGotReply = new AtomicBoolean();
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem(ActorSystemConfig.builder().serverMode());
		system.deployPods(factory, new PodConfiguration(domain, factory.getClass().getName(), 1, 1));
		system.addActor(() -> new Actor("backend") {
			@Override
			public void preStart() {
				setAlias("regression-backend");
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1)
					tell("done", 2, message.source(), message.interaction());
				else if (message.tag()==42)
					backendGotReply.set(true);
			}
		});
		ActorId client = system.addActor(() -> new Actor("client") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==42) {
					reply.set(message.value());
					replyInteraction.set(message.interaction());
					testDone.countDown();
				}
			}
		});
		system.start();
		system.sendViaAlias(ActorMessage.create(null, 1, client, null, interaction, null, null), domain);

		await(testDone);
		assertEquals("done", reply.get());
		assertEquals(interaction, replyInteraction.get()); // sender gets back its own interaction (also null)
		assertFalse(backendGotReply.get());
	}

	@Test(timeout=5000)
	public void test_function_pod_pending_reply() {
		pendingReplyToOriginalSender(() -> new PendingFunctionPod(), "regression-pending-function", UUID.randomUUID());
	}

	@Test(timeout=5000)
	public void test_function_pod_pending_reply_without_interaction() {
		pendingReplyToOriginalSender(() -> new PendingFunctionPod(), "regression-pending-function", null);
	}

	@Test(timeout=5000)
	public void test_remote_function_pod_pending_reply_local_sender() {
		pendingReplyToOriginalSender(() -> new PendingRemoteFunctionPod(), "regression-pending-remote-function", UUID.randomUUID());
	}

	@Test(timeout=5000)
	public void test_remote_function_pod_pending_reply_local_sender_without_interaction() {
		pendingReplyToOriginalSender(() -> new PendingRemoteFunctionPod(), "regression-pending-remote-function", null);
	}

	// SDK FunctionPod/RemoteFunctionPod: NO_REPLY -> replyAllowed==false, interaction is a real key (not NO_REPLY),
	// no reply to the sender (direct and pending); a following request with interaction is answered normally
	protected void noReplyIsSuppressed(PodFactory factory, String domain, int tag) throws InterruptedException {
		List<UUID> replies = new CopyOnWriteArrayList<>();
		CountDownLatch backendDone = new CountDownLatch(1);
		CountDownLatch controlDone = new CountDownLatch(1);
		UUID control = UUID.randomUUID();
		functionLog.clear();

		createSystem(ActorSystemConfig.builder().serverMode());
		system.deployPods(factory, new PodConfiguration(domain, factory.getClass().getName(), 1, 1));
		system.addActor(() -> new Actor("backend") {
			@Override
			public void preStart() {
				setAlias("regression-backend");
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==1) {
					tell("done", 2, message.source(), message.interaction());
					backendDone.countDown();
				}
			}
		});
		ActorId client = system.addActor(() -> new Actor("client") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==42) {
					replies.add(message.interaction());
					if (control.equals(message.interaction()))
						controlDone.countDown();
				}
			}
		});
		system.start();
		system.sendViaAlias(ActorMessage.create(null, tag, client, null, ActorMessage.NO_REPLY, null, null), domain);
		if (tag==1) {
			await(backendDone);
			Thread.sleep(200); // late reply of the backend is processed by the pod
		}
		system.sendViaAlias(ActorMessage.create(null, 3, client, null, control, null, null), domain);

		await(controlDone);
		Thread.sleep(100);
		assertEquals(List.of(control), replies);
		assertNotEquals(ActorMessage.NO_REPLY, functionLog.get(0));
		assertEquals(Boolean.FALSE, functionLog.get(1));
		assertEquals(List.of(control, Boolean.TRUE), functionLog.subList(2, 4));
	}

	@Test(timeout=5000)
	public void test_function_pod_no_reply_direct() throws InterruptedException {
		noReplyIsSuppressed(() -> new PendingFunctionPod(), "regression-pending-function", 3);
	}

	@Test(timeout=5000)
	public void test_function_pod_no_reply_pending() throws InterruptedException {
		noReplyIsSuppressed(() -> new PendingFunctionPod(), "regression-pending-function", 1);
	}

	@Test(timeout=5000)
	public void test_remote_function_pod_no_reply_direct() throws InterruptedException {
		noReplyIsSuppressed(() -> new PendingRemoteFunctionPod(), "regression-pending-remote-function", 3);
	}

	@Test(timeout=5000)
	public void test_remote_function_pod_no_reply_pending() throws InterruptedException {
		noReplyIsSuppressed(() -> new PendingRemoteFunctionPod(), "regression-pending-remote-function", 1);
	}

	// SDK RemoteFunctionPod: direct reply to a RemotePodMessage with reply()==true goes via the InternalServerCallback,
	// payload "reject" returns no done reply and must be answered with CONFLICT
	public static class DirectRemoteFunctionPod extends RemoteFunctionPod {
		@Override
		public PodRemoteFunction createFunction(ActorRef host, PodContext context) {
			return new PodRemoteFunction(host, context) {
				@Override
				public Reply handle(ActorMessage<?> message, UUID interaction, boolean replyAllowed) {
					return null;
				}

				@Override
				public Reply handle(RemotePodMessage remoteMessage, UUID interaction, boolean replyExpected) {
					return "reject".equals(remoteMessage.remotePodMessageDTO().payload()) ? Reply.none() : Reply.of("hello", 42);
				}
			};
		}

		@Override
		public String domain() {
			return "regression-direct-remote-function";
		}
	}

	protected int directRemoteReplyTag(String payload) {
		AtomicInteger tag = new AtomicInteger(Integer.MIN_VALUE);
		CountDownLatch testDone = new CountDownLatch(1);

		createSystem(ActorSystemConfig.builder().serverMode());
		system.deployPods(() -> new DirectRemoteFunctionPod(),
			new PodConfiguration("regression-direct-remote-function", DirectRemoteFunctionPod.class.getName(), 1, 1));
		ActorId client = system.addActor(() -> new Actor("remote-client") {
			@Override
			public void preStart() {
				expose();
			}

			@Override
			public void receive(ActorMessage<?> message) {
				tag.set(message.tag());
				testDone.countDown();
			}
		});
		InternalServerCallback internalServerCallback = (replyAddress, result, replyTag)
			-> ((ActorService)system).sendAsServer(ActorMessage.create(result, replyTag, system.SYSTEM_ID(), GlobalId.of((String)replyAddress)));
		((InternalActorSystem)system).setRuntimeConfig(ActorRuntimeConfig.builder().internalServerCallback(internalServerCallback).build());
		system.start();

		RemotePodMessage remotePodMessage = new RemotePodMessage(
			new RemotePodMessageDTO(payload, 0, "regression-direct-remote-function", true), client.globalId().toString(), null);
		system.sendViaAlias(ActorMessage.create(remotePodMessage, 0, system.SYSTEM_ID(), null), "regression-direct-remote-function");

		await(testDone);
		return tag.get();
	}

	@Test(timeout=5000)
	public void test_remote_function_pod_direct_reply() {
		assertEquals(42, directRemoteReplyTag("ok"));
	}

	@Test(timeout=5000)
	public void test_remote_function_pod_direct_rejected_reply() {
		assertEquals(PodStatus.CONFLICT, directRemoteReplyTag("reject"));
	}

	// runtimes (loom report H2): with metricsEnabled but without processing time measurement,
	// metrics() must still process the message (missing else branch dropped all messages)
	@Test(timeout=5000)
	public void test_metrics_enabled_without_processing_time() {
		CountDownLatch testDone = new CountDownLatch(2);

		createSystem();
		system.getConfig().metricsEnabled().set(true);
		system.getConfig().trackRequestRatePerActor().set(true);
		ActorId dest = system.addActor(() -> new Actor("metrics") {
			@Override
			public void receive(ActorMessage<?> message) {
				testDone.countDown();
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), dest));
		system.send(ActorMessage.create(null, 2, system.SYSTEM_ID(), dest));

		await(testDone);
	}

	// classic report M1: an actor under permanent load (sends to itself) must not starve other actors,
	// neither for external messages nor for messages sent by the flooding actor itself
	@Test(timeout=10000)
	public void test_fairness_under_permanent_load() throws InterruptedException {
		AtomicBoolean flooding = new AtomicBoolean(true);
		AtomicInteger loops = new AtomicInteger();
		CountDownLatch externalDone = new CountDownLatch(1);
		CountDownLatch internalDone = new CountDownLatch(1);

		createSystem(ActorSystemConfig.builder().parallelism(1).parallelismFactor(1));
		ActorId external = system.addActor(() -> new Actor("fairness-external") {
			@Override
			public void receive(ActorMessage<?> message) {
				externalDone.countDown();
			}
		});
		ActorId internal = system.addActor(() -> new Actor("fairness-internal") {
			@Override
			public void receive(ActorMessage<?> message) {
				internalDone.countDown();
			}
		});
		ActorId flood = system.addActor(() -> new Actor("fairness-flood") {
			@Override
			public void receive(ActorMessage<?> message) {
				if (loops.incrementAndGet()==1_000)
					tell(null, 2, internal);
				if (flooding.get())
					tell(null, 1, self());
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), flood));
		Thread.sleep(200);
		system.send(ActorMessage.create(null, 1, system.SYSTEM_ID(), external));

		try {
			await(externalDone);
			await(internalDone);
		}
		finally {
			flooding.set(false);
		}
	}
}
