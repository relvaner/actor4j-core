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
package io.actor4j.core.actors;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorMessageMatcher;

public class HandlerActor extends EmbeddedHostActor {
	protected ActorMessageMatcher matcher;
	
	public HandlerActor() {
		this(null, false);
	}

	public HandlerActor(boolean redirectEnabled) {
		this(null, redirectEnabled);
	}

	public HandlerActor(String name, boolean redirectEnabled) {
		super(name, redirectEnabled);
		
		matcher = new ActorMessageMatcher();
	}

	public HandlerActor(String name) {
		this(name, false);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (!matcher.apply(message))
			unhandled(message);
	}
	
	public void handle(ActorMessage<?> message, BiConsumer<ActorMessage<?>, EmbeddedActor> handler, Predicate<ActorMessage<?>> done) {
		UUID id = message.interaction();
		EmbeddedActor embeddedActor = router.get(id);
		if (embeddedActor!=null)
			embeddedActor.embedded(message);
		else {
			embeddedActor = new EmbeddedActor("", this, id) {
				@Override
				public boolean receive(ActorMessage<?> message) {
					handler.accept(message, this);
					
					return true;
				}
			};
			addEmbeddedChild(embeddedActor);
			embeddedActor.embedded(message);
		}
		if (done.test(message))
			removeEmbeddedChild(embeddedActor);
	}
	
	public void handle(ActorMessage<?> message, BiFunction<EmbeddedHostActor, UUID, EmbeddedActor> factory) {
		boolean done = false;
		UUID id = message.interaction();
		EmbeddedActor embeddedActor = router.get(id);
		if (embeddedActor!=null)
			done = embeddedActor.embedded(message);
		else {
			embeddedActor = factory.apply(this, id);
			addEmbeddedChild(embeddedActor);
			done = embeddedActor.embedded(message);
		}
		if (done)
			removeEmbeddedChild(embeddedActor);
	}
}
