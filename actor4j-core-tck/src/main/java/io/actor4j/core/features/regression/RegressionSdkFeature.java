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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.id.GlobalId;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.mutable.MutableInt;
import io.actor4j.core.utils.ActorMessageMatcher;
import io.actor4j.core.utils.ActorMessageMatcherHandler;
import io.actor4j.core.utils.CacheLRU;
import io.actor4j.core.utils.CacheVolatileLRU;
import io.actor4j.core.utils.CircuitBreaker;
import io.actor4j.core.utils.ConcurrentActorGroupSet;
import io.actor4j.core.utils.EmbeddedActorFactory;

/**
 * Regression tests for fixes in actor4j-core-sdk (review 2026-10).
 * These tests do not need a running actor system.
 * AI-maintained.
 */
public class RegressionSdkFeature {
	protected static EmbeddedHostActor createHost() {
		return new EmbeddedHostActor("host") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		};
	}

	// SDK H1, H2: stopping several (unnamed and named) embedded actors
	@Test(timeout=5000)
	public void test_embedded_postStop_with_several_children() {
		EmbeddedActorFactory unnamed = () -> new EmbeddedActor() {
			@Override
			public boolean receive(ActorMessage<?> message) {
				return true;
			}
		};
		EmbeddedActorFactory named = () -> new EmbeddedActor("named") {
			@Override
			public boolean receive(ActorMessage<?> message) {
				return true;
			}
		};

		EmbeddedHostActor host = createHost();
		host.addEmbeddedChild(unnamed);
		host.addEmbeddedChild(unnamed);
		host.addEmbeddedChild(named);
		host.postStop();

		assertEquals(0, host.getRouter().size());
		assertEquals(0, host.getHandlerRouter().size());
	}

	// SDK N3: unbecome() on an empty behaviour stack
	@Test(timeout=5000)
	public void test_embedded_unbecome_on_empty_stack() {
		AtomicReference<EmbeddedActor> embedded = new AtomicReference<>();
		EmbeddedHostActor host = createHost();
		host.addEmbeddedChild(() -> {
			EmbeddedActor actor = new EmbeddedActor() {
				@Override
				public boolean receive(ActorMessage<?> message) {
					return true;
				}
			};
			embedded.set(actor);
			return actor;
		});

		embedded.get().unbecome();
		embedded.get().unbecome();
	}

	// SDK H3: CacheVolatileLRU.evict() must keep fresh entries
	@Test(timeout=5000)
	public void test_cache_volatile_evict_keeps_fresh_entries() {
		CacheVolatileLRU<String, String> cache = new CacheVolatileLRU<>(10);
		cache.put("A", "AA");
		cache.evict(60_000);

		assertTrue(cache.containsKey("A"));
	}

	// SDK N5: CacheLRU with size 0 and LRU order of get(List)
	@Test(timeout=5000)
	public void test_cache_lru() {
		CacheLRU<String, String> empty = new CacheLRU<>(0);
		empty.put("A", "AA");
		assertFalse(empty.containsKey("A"));

		CacheLRU<String, String> cache = new CacheLRU<>(3);
		cache.put("A", "AA");
		cache.put("B", "BB");
		cache.put("C", "CC");
		cache.get(List.of("A")); // access must update the LRU order
		cache.put("D", "DD");    // evicts the least recently used entry (B)

		assertTrue(cache.containsKey("A"));
		assertFalse(cache.containsKey("B"));
		assertTrue(cache.containsKey("C"));
		assertTrue(cache.containsKey("D"));
	}

	// SDK H5: ImmutableList must be a real copy
	@Test(timeout=5000)
	public void test_immutable_list_is_copy() {
		List<String> source = new ArrayList<>(List.of("a"));
		ActorMessage<?> copy = ActorMessage.create(ImmutableList.of(source), 0, null, null).copy();
		source.add("b");

		assertEquals(List.of("a"), ((ImmutableList<?>)copy.value()).get());
	}

	// SDK H6: Optional getters must handle null fields
	@Test(timeout=5000)
	public void test_message_matcher_handler_optional() {
		ActorMessageMatcherHandler handler = new ActorMessageMatcherHandler();
		UUID interaction = UUID.randomUUID();
		handler.define(interaction, ActorMessage.create(null, 1, null, null), new ActorMessageMatcher());

		assertEquals(Optional.empty(), handler.source(interaction));
		assertEquals(Optional.empty(), handler.dest(interaction));
		assertEquals(Optional.empty(), handler.protocol(interaction));
		assertEquals(Optional.empty(), handler.domain(interaction));
	}

	// SDK M2: enum values must be shareable without copy
	@Test(timeout=5000)
	public void test_message_copy_with_enum_value() {
		ActorMessage<?> message = ActorMessage.create(TimeUnit.SECONDS, 0, null, null);

		assertSame(message, message.copy());
	}

	// SDK M8: none() and redirect ids must support equals
	@Test(timeout=5000)
	public void test_actor_id_equals() {
		assertEquals(ActorId.none(), ActorId.none());

		ActorId dest = GlobalId.random();
		assertEquals(ActorId.ofRedirect(dest), ActorId.ofRedirect(dest));
		assertEquals(ActorId.ofRedirect(dest).hashCode(), ActorId.ofRedirect(dest).hashCode());
	}

	// SDK M11: return values of the mutable number types
	@Test(timeout=5000)
	public void test_mutable_int_return_values() {
		MutableInt value = new MutableInt(0);

		assertEquals(Integer.valueOf(0), value.getAndIncrement());
		assertEquals(Integer.valueOf(2), value.incrementAndGet());
		assertEquals(5, value.addAndGet(3));
	}

	// SDK M12: ConcurrentActorGroupSet must support add() and duplicates
	@Test(timeout=5000)
	public void test_concurrent_actor_group_set() {
		ActorId id = GlobalId.random();

		ConcurrentActorGroupSet fromCollection = new ConcurrentActorGroupSet(List.of(id, id));
		assertEquals(1, fromCollection.size());
		assertTrue(fromCollection.add(GlobalId.random()));

		ConcurrentActorGroupSet withCapacity = new ConcurrentActorGroupSet(16);
		assertTrue(withCapacity.add(id));
	}

	// SDK N15: only one trial call in state HALF_OPEN
	@Test(timeout=5000)
	public void test_circuit_breaker_single_trial_call() {
		CircuitBreaker breaker = new CircuitBreaker(1, 0);
		breaker.failure();

		assertTrue(breaker.isCallable());  // trial call
		assertFalse(breaker.isCallable()); // no further calls until success/failure

		breaker.success();
		assertTrue(breaker.isCallable());
		assertTrue(breaker.isClosed());
	}
}
