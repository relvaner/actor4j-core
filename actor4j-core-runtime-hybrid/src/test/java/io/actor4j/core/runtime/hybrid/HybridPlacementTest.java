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
package io.actor4j.core.runtime.hybrid;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.HybridActorRuntime;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.PooledActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.config.HybridActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorThread;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.classic.ClassicActorCell;

// mixed placement: thread-bound and pooled actors exchange messages, stop and restart across the boundary
public class HybridPlacementTest {
	protected static abstract class Pooled extends Actor implements PooledActor {
		public Pooled(String name) {
			super(name);
		}
	}

	@Test(timeout=10_000)
	public void test_placement_and_ping_pong() throws InterruptedException {
		ActorSystem system = HybridActorRuntime.create();
		int rounds = 10_000;
		CountDownLatch done = new CountDownLatch(1);
		Map<String, String> threads = new ConcurrentHashMap<>();

		ActorId pooled = system.addActor(() -> new Pooled("pooled") {
			@Override
			public void receive(ActorMessage<?> message) {
				threads.putIfAbsent("pooled", Thread.currentThread().getName());
				tell(null, message.tag()+1, message.source());
			}
		});
		ActorId bound = system.addActor(() -> new Actor("bound") {
			@Override
			public void receive(ActorMessage<?> message) {
				threads.putIfAbsent("bound", Thread.currentThread().getName());
				if (message.tag()<rounds)
					tell(null, message.tag()+1, pooled);
				else
					done.countDown();
			}
		});

		assertTrue(pooled instanceof ClassicActorCell);
		assertFalse(bound instanceof ClassicActorCell);

		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), bound));
		assertTrue("ping-pong across the boundary", done.await(5, TimeUnit.SECONDS));

		assertTrue(threads.get("bound"), threads.get("bound").startsWith("actor4j-worker-thread"));
		assertFalse(threads.get("pooled"), threads.get("pooled").startsWith("actor4j-worker-thread"));

		system.shutdownWithActors(true);
	}

	@Test(timeout=10_000)
	public void test_pooled_children_and_stop() throws InterruptedException {
		ActorSystem system = HybridActorRuntime.create();
		CountDownLatch stopped = new CountDownLatch(2);
		CountDownLatch received = new CountDownLatch(1);

		ActorId parent = system.addActor(() -> new Actor("bound-parent") {
			protected ActorId child;

			@Override
			public void preStart() {
				child = addChild(() -> new Pooled("pooled-child") {
					@Override
					public void receive(ActorMessage<?> message) {
						tell(null, 1, getParent());
					}

					@Override
					public void postStop() {
						stopped.countDown();
					}
				});
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag()==0)
					tell(null, 0, child);
				else
					received.countDown();
			}

			@Override
			public void postStop() {
				stopped.countDown();
			}
		});

		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		assertTrue("pooled child answers", received.await(5, TimeUnit.SECONDS));

		system.send(ActorMessage.create(null, Actor.STOP, system.SYSTEM_ID(), parent));
		assertTrue("parent and pooled child stopped", stopped.await(5, TimeUnit.SECONDS));

		system.shutdownWithActors(true);
	}

	protected static long actorThreads(String name) {
		return Thread.getAllStackTraces().keySet().stream()
			.filter(t -> t instanceof ActorThread && t.isAlive() && t.getName().startsWith(name+"-worker-thread-")).count();
	}
	
	protected void assertThreads(ActorSystemConfig.Builder<?> builder, int pooled, int bound) throws InterruptedException {
		String name = "hybrid-"+pooled+"-"+bound+"-"+System.nanoTime();
		ActorSystem system = HybridActorRuntime.create(builder.name(name).build());
		system.start();
		HybridActorExecutorService executor = (HybridActorExecutorService)((InternalActorSystem)system).getExecutorService();
		assertEquals(pooled, executor.pooledThreads());
		assertEquals(bound, executor.threadBoundThreads());
		assertEquals(bound, actorThreads(name));
		system.shutdown(true);
	}
	
	@Test(timeout=10_000)
	public void test_pooled_parallelism() throws InterruptedException {
		assertThreads(ActorSystemConfig.builder().parallelism(8), 4, 4);                       // automatic: half
		assertThreads(HybridActorSystemConfig.builder().pooledParallelism(2).parallelism(8), 2, 6);
		assertThreads(ActorSystemConfig.builder().parallelism(4).parallelismFactor(2), 4, 4);  // from parallelism*factor
		assertThreads(HybridActorSystemConfig.builder().pooledParallelism(20).parallelism(8), 7, 1); // at least one thread-bound
		assertThreads(ActorSystemConfig.builder().parallelism(1), 1, 1);                       // at least one for each pool
		assertThreads(HybridActorSystemConfig.builder(HybridActorSystemConfig.builder().pooledParallelism(3).build()).parallelism(8), 3, 5); // copied config keeps it
	}
}
