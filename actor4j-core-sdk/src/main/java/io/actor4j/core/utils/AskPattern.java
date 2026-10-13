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
package io.actor4j.core.utils;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorSystem;

/**
 * Per request a temporary actor receives the answer (first message) or its timeout, then stops itself.
 * askAsync never blocks; ask blocks and must not be used inside an actor.
 */
public final class AskPattern {
	private static final class AskPatternResourceActor extends ResourceActor {
		private final CompletableFuture<ActorMessage<?>> future;
		private final Duration timeout;
		private final UUID timeoutId;
		private ScheduledFuture<?> scheduledTimeout;

		public AskPatternResourceActor(CompletableFuture<ActorMessage<?>> future, Duration timeout) {
			super(null, true, false);
			this.future = future;
			this.timeout = timeout;
			this.timeoutId = UUID.randomUUID();
		}

		@Override
		public void preStart() {
			if (timeout!=null)
				scheduledTimeout = getSystem().globalTimer().scheduleOnce(
					ActorMessage.create(null, TIMEOUT, self(), null, timeoutId), self(), timeout.toNanos(), TimeUnit.NANOSECONDS);
		}

		@Override
		public void receive(ActorMessage<?> message) {
			if (!future.isDone()) {
				if (message.tag()==TIMEOUT && timeoutId.equals(message.interaction()))
					future.completeExceptionally(new TimeoutException());
				else {
					if (scheduledTimeout!=null)
						scheduledTimeout.cancel(false);
					future.complete(message);
				}
				stop();
			}
		}
	}

	public static final class AskPatternException extends RuntimeException {
		private static final long serialVersionUID = 1289900543681552734L;
	}

	public static CompletableFuture<ActorMessage<?>> askAsync(ActorMessage<?> message, ActorSystem system) {
		return internal_askAsync(message, null, null, system);
	}

	public static CompletableFuture<ActorMessage<?>> askAsync(ActorMessage<?> message, Duration timeout, ActorSystem system) {
		return internal_askAsync(message, null, timeout, system);
	}

	public static CompletableFuture<ActorMessage<?>> askAsync(ActorMessage<?> message, long timeout, TimeUnit unit, ActorSystem system) {
		return internal_askAsync(message, null, Duration.of(timeout, unit.toChronoUnit()), system);
	}

	public static CompletableFuture<ActorMessage<?>> askAsync(ActorMessage<?> message, String alias, ActorSystem system) {
		return internal_askAsync(message, alias, null, system);
	}

	public static CompletableFuture<ActorMessage<?>> askAsync(ActorMessage<?> message, String alias, Duration timeout, ActorSystem system) {
		return internal_askAsync(message, alias, timeout, system);
	}

	public static CompletableFuture<ActorMessage<?>> askAsync(ActorMessage<?> message, String alias, long timeout, TimeUnit unit, ActorSystem system) {
		return internal_askAsync(message, alias, Duration.of(timeout, unit.toChronoUnit()), system);
	}

	private static CompletableFuture<ActorMessage<?>> internal_askAsync(ActorMessage<?> message, String alias, Duration timeout, ActorSystem system) {
		if (!((InternalActorSystem)system).getExecutorService().isStarted())
			throw new AskPatternException();

		CompletableFuture<ActorMessage<?>> future = new CompletableFuture<>();
		ActorId source = system.addActor(() -> new AskPatternResourceActor(future, timeout));

		if (alias!=null)
			system.sendViaAlias(message.shallowCopy(source, message.dest()), alias);
		else
			system.send(message.shallowCopy(source, message.dest()));

		return future;
	}

	public static Optional<ActorMessage<?>> ask(ActorMessage<?> message, ActorSystem system) {
		return await(askAsync(message, system));
	}

	public static Optional<ActorMessage<?>> ask(ActorMessage<?> message, Duration timeout, ActorSystem system) {
		return await(askAsync(message, timeout, system));
	}

	public static Optional<ActorMessage<?>> ask(ActorMessage<?> message, long timeout, TimeUnit unit, ActorSystem system) {
		return await(askAsync(message, timeout, unit, system));
	}

	private static Optional<ActorMessage<?>> await(CompletableFuture<ActorMessage<?>> future) {
		try {
			return Optional.of(future.get());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}