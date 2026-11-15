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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;

// convenience: actor with a BehaviourContext, the behaviour is the return value
// (Behaviour.same(), Behaviour.unhandled(), Behaviour.stopped() or another behaviour, e.g. paused())
public abstract class BehaviourActor extends Actor {
	protected final ActorBehaviourContext behaviours = new ActorBehaviourContext(this, this::initial);

	public BehaviourActor() {
		this(null);
	}

	public BehaviourActor(String name) {
		super(name);
	}

	// first behaviour, also after a restart (a restart creates a new instance)
	protected abstract ActorBehaviour initial();

	public String state() {
		return behaviours.state();
	}

	@Override
	public final void receive(ActorMessage<?> message) {
		behaviours.receive(message);
	}

	protected ActorBehaviour await(Predicate<ActorMessage<?>> predicate, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action) {
		return behaviours.await(predicate, otherwise, action);
	}

	protected ActorBehaviour await(int tag, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action) {
		return behaviours.await(tag, otherwise, action);
	}

	protected ActorBehaviour await(int tag, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action,
			long timeout, TimeUnit unit, Supplier<ActorBehaviour> onTimeout) {
		return behaviours.await(tag, otherwise, action, timeout, unit, onTimeout);
	}

	// without otherwise: other messages go to the behaviour current when await was called
	protected ActorBehaviour await(int tag, Function<ActorMessage<?>, ActorBehaviour> action) {
		return behaviours.await(tag, action);
	}

	protected ActorBehaviour await(int tag, Function<ActorMessage<?>, ActorBehaviour> action,
			long timeout, TimeUnit unit, Supplier<ActorBehaviour> onTimeout) {
		return behaviours.await(tag, action, timeout, unit, onTimeout);
	}

	// the behaviour is the return value: become/unbecome would bypass it
	@Override
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		throw new UnsupportedOperationException("become is not supported by a BehaviourActor, return the next behaviour instead");
	}

	@Override
	public void unbecome() {
		throw new UnsupportedOperationException("unbecome is not supported by a BehaviourActor, return the next behaviour instead");
	}

	@Override
	public void unbecomeAll() {
		throw new UnsupportedOperationException("unbecomeAll is not supported by a BehaviourActor, return the next behaviour instead");
	}
}
