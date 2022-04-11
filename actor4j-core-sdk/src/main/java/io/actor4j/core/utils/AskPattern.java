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
package io.actor4j.core.utils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorSystem;

public final class AskPattern {
	private static final class AskPatternRessourceActor extends ResourceActor {
		private CompletableFuture<ActorMessage<?>> future;
		
		public AskPatternRessourceActor(CompletableFuture<ActorMessage<?>> future) {
			super(null, false, false);
			this.future = future;
		}
		
		@Override
		public void receive(ActorMessage<?> message) {
			future.complete(message);
		}
	}
	
	public static final class AskPatternException extends RuntimeException {
		private static final long serialVersionUID = 1289900543681552734L;
	}
	
	public static Optional<ActorMessage<?>> ask(ActorMessage<?> message, ActorSystem system) {
		if (!((InternalActorSystem)system).getExecuterService().isStarted())
			throw new AskPatternException();
		
		CompletableFuture<ActorMessage<?>> future = new CompletableFuture<>();

		UUID source = system.addActor(() -> new AskPatternRessourceActor(future));
		system.send(message.shallowCopy(source, message.dest()));

		ActorMessage<?> result = null;
		boolean exception = false;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			exception = true;
		}
		system.send(ActorMessage.create(null, Actor.POISONPILL, system.SYSTEM_ID(), source));

		return exception ? Optional.empty() : Optional.of(result);
	}
	
	public static Optional<ActorMessage<?>> ask(ActorMessage<?> message, long timeout, TimeUnit unit, ActorSystem system) {
		if (!((InternalActorSystem)system).getExecuterService().isStarted())
			throw new AskPatternException();
		
		CompletableFuture<ActorMessage<?>> future = new CompletableFuture<>();
		
		UUID source = system.addActor(() -> new AskPatternRessourceActor(future));
		system.send(message.shallowCopy(source, message.dest()));

		ActorMessage<?> result = null;
		boolean exception = false;
		try {
			result = future.get(timeout, unit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			exception = true;
		}
		system.send(ActorMessage.create(null, Actor.POISONPILL, system.SYSTEM_ID(), source));

		return exception ? Optional.empty() : Optional.of(result);
	}
}
