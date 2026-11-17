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
package io.actor4j.core.actors.behaviour;

import java.util.function.Predicate;

import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.messages.ActorMessage;

// convenience: embedded actor with an EmbeddedActorBehaviourContext, the behaviour is the return value,
// stopped() ends the embedded actor (receive returns true). Waiting with behaviours.await(...), the await
// methods of EmbeddedActor are based on become and not supported.
public abstract class BehaviourEmbeddedActor extends EmbeddedActor {
	protected final EmbeddedActorBehaviourContext behaviours = new EmbeddedActorBehaviourContext(this, this::initial);

	public BehaviourEmbeddedActor() {
		super();
	}

	public BehaviourEmbeddedActor(String name) {
		super(name);
	}

	// first behaviour
	protected abstract ActorBehaviour initial();

	public String state() {
		return behaviours.state();
	}

	@Override
	public final boolean receive(ActorMessage<?> message) {
		return behaviours.receive(message);
	}

	// the behaviour is the return value: become/unbecome would bypass it
	@Override
	public void become(Predicate<ActorMessage<?>> behaviour, boolean replace) {
		throw new UnsupportedOperationException("become is not supported by a BehaviourEmbeddedActor, return the next behaviour instead");
	}

	@Override
	public void unbecome() {
		throw new UnsupportedOperationException("unbecome is not supported by a BehaviourEmbeddedActor, return the next behaviour instead");
	}

	@Override
	public void unbecomeAll() {
		throw new UnsupportedOperationException("unbecomeAll is not supported by a BehaviourEmbeddedActor, return the next behaviour instead");
	}
}
