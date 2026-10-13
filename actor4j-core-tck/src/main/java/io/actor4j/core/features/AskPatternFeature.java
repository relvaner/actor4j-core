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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.AskPattern;
import io.actor4j.core.utils.AskPattern.AskPatternException;

public class AskPatternFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(1)
			.build();
		system = ActorSystem.create(AllFeaturesTest.factory(), config);
	}

	@Test(timeout=5000)
	public void test() {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				tell("done", 200, message.source());
			} 
		});
		
		system.start();
		
		Optional<ActorMessage<?>> optional = AskPattern.ask(ActorMessage.create(null, 0, null, dest), system);
		ActorMessage<?> message = optional.get();
		assertEquals(200, message.tag());
		assertEquals("done", message.valueAsString());
		
		system.shutdown(true);
	}
	
	@Test(expected=AskPatternException.class)
	public void test_exception() {
		AskPattern.ask(ActorMessage.create(null, 0, null, ActorId.none()), system);
	}
	
	@Test(timeout=5000)
	public void test_timeout() throws TimeoutException {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			} 
		});
		
		system.start();
		
		Optional<ActorMessage<?>> optional = AskPattern.ask(ActorMessage.create(null, 0, null, dest), 500, TimeUnit.MILLISECONDS, system);
		assertFalse(optional.isPresent());
		
		system.shutdown(true);
	}
	
	@Test(timeout=5000)
	public void test_async() throws Exception {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				tell("done", 200, message.source());
			}
		});

		system.start();

		CompletableFuture<ActorMessage<?>> future = AskPattern.askAsync(ActorMessage.create(null, 0, null, dest), Duration.ofSeconds(2), system);
		ActorMessage<?> message = future.get();
		assertEquals(200, message.tag());
		assertEquals("done", message.valueAsString());

		system.shutdown(true);
	}

	@Test(timeout=5000)
	public void test_async_alias() throws Exception {
		system.setAlias(system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				tell("done via alias", 201, message.source());
			}
		}), "ask-alias");

		system.start();

		ActorMessage<?> message = AskPattern.askAsync(ActorMessage.create(null, 0, null, null), "ask-alias", Duration.ofSeconds(2), system).get();
		assertEquals(201, message.tag());
		assertEquals("done via alias", message.valueAsString());

		system.shutdown(true);
	}

	@Test(timeout=5000)
	public void test_async_timeout() throws Exception {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});

		system.start();

		CompletableFuture<ActorMessage<?>> future = AskPattern.askAsync(ActorMessage.create(null, 0, null, dest), Duration.ofMillis(300), system);
		ExecutionException e = assertThrows(ExecutionException.class, () -> future.get());
		assertTrue(e.getCause() instanceof TimeoutException);

		system.shutdown(true);
	}

	@Test(timeout=5000)
	public void test_async_timeout_unit() throws Exception {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});

		system.start();

		CompletableFuture<ActorMessage<?>> future = AskPattern.askAsync(ActorMessage.create(null, 0, null, dest), 300, TimeUnit.MILLISECONDS, system);
		ExecutionException e = assertThrows(ExecutionException.class, () -> future.get());
		assertTrue(e.getCause() instanceof TimeoutException);

		system.shutdown(true);
	}

	@Test(timeout=5000)
	public void test_timeout_duration() {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		});

		system.start();

		Optional<ActorMessage<?>> optional = AskPattern.ask(ActorMessage.create(null, 0, null, dest), Duration.ofMillis(300), system);
		assertFalse(optional.isPresent());

		system.shutdown(true);
	}

	@Test(timeout=5000)
	public void test_async_parallel() throws Exception {
		ActorId dest = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				tell(message.value(), message.tag(), message.source());
			}
		});

		system.start();

		List<CompletableFuture<ActorMessage<?>>> futures = new ArrayList<>();
		for (int i=0; i<20; i++)
			futures.add(AskPattern.askAsync(ActorMessage.create("value-" + i, i, null, dest), Duration.ofSeconds(2), system));
		for (int i=0; i<20; i++) {
			ActorMessage<?> message = futures.get(i).get();
			assertEquals(i, message.tag());
			assertEquals("value-" + i, message.valueAsString());
		}

		system.shutdown(true);
	}

	@Test(expected=AskPatternException.class)
	public void test_async_exception() {
		AskPattern.askAsync(ActorMessage.create(null, 0, null, ActorId.none()), Duration.ofSeconds(1), system);
	}
}
