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
import java.util.function.Predicate;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.embedded.InternalEmbeddedActorCell;
import io.actor4j.core.utils.ActorMessageMatcher;
import io.actor4j.core.utils.EmbeddedActorFactory;

public class EmbeddedHandlerActor extends EmbeddedHostActor {
	protected ActorMessageMatcher matcher;
	
	public EmbeddedHandlerActor() {
		this(null, false, false);
	}

	public EmbeddedHandlerActor(boolean redirectEnabled) {
		this(null, redirectEnabled, false);
	}

	public EmbeddedHandlerActor(String name, boolean redirectEnabled, boolean messageQueueEnabled) {
		super(name, redirectEnabled, messageQueueEnabled);
		
		matcher = new ActorMessageMatcher();
	}

	public EmbeddedHandlerActor(String name) {
		this(name, false, false);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (!matcher.apply(message))
			unhandled(message);
	}
	
	public void handle(ActorMessage<?> message, BiConsumer<ActorMessage<?>, EmbeddedActorRef> handler, Predicate<ActorMessage<?>> done) {
		handle(message, null, handler, done);
	}
	
	public void handle(ActorMessage<?> message, Runnable onCreate, BiConsumer<ActorMessage<?>, EmbeddedActorRef> handler, Predicate<ActorMessage<?>> done) {
		UUID id = message.interaction();
		InternalEmbeddedActorCell embeddedActorCell = getRouter().get(id);
		if (embeddedActorCell!=null)
			embeddedActorCell.embedded(message);
		else {
			if (onCreate!=null)
				onCreate.run();
			UUID embeddedActorCellId = addEmbeddedChild(() -> new EmbeddedActor() {
				@Override
				public boolean receive(ActorMessage<?> message) {
					handler.accept(message, this);
					
					return true;
				}
			}, id);
			embeddedActorCell = getRouter().get(embeddedActorCellId);
			embeddedActorCell.embedded(message);
		}
		if (done.test(message) && embeddedActorCell!=null)
			removeEmbeddedChild(embeddedActorCell.getId());
	}
	
	public void handle(ActorMessage<?> message, EmbeddedActorFactory factory) {
		boolean done = false;
		UUID id = message.interaction();
		InternalEmbeddedActorCell embeddedActorCell = getRouter().get(id);
		if (embeddedActorCell!=null)
			done = embeddedActorCell.embedded(message);
		else {
			UUID embeddedActorCellId = addEmbeddedChild(factory, id);
			embeddedActorCell = getRouter().get(embeddedActorCellId);
			done = embeddedActorCell.embedded(message);
		}
		if (done && embeddedActorCell!=null)
			removeEmbeddedChild(embeddedActorCell.getId());
	}
}
