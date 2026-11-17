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

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;

// current behaviour of an actor and the evaluation of the return value, usable by any actor (composition):
// 	protected final ActorBehaviourContext behaviours = new ActorBehaviourContext(this, this::initial);
// 	public void receive(ActorMessage<?> message) { behaviours.receive(message); }
public class ActorBehaviourContext extends AbstractBehaviourContext {
	protected final ActorRef ref;

	public ActorBehaviourContext(ActorRef ref, Supplier<ActorBehaviour> initial) {
		super(initial);
		this.ref = ref;
	}

	public void receive(ActorMessage<?> message) {
		ActorBehaviour result = next(message);

		if (result==ActorBehaviour.unhandled())
			((Actor)ref).unhandled(message);
		else if (result==ActorBehaviour.stopped())
			((Actor)ref).stop();
	}

	@Override
	protected ScheduledFuture<?> scheduleTimeout(UUID timeoutId, long timeout, TimeUnit unit) {
		return ref.getSystem().globalTimer().scheduleOnce(
			ActorMessage.create(null, Actor.TIMEOUT, ref.self(), null, timeoutId), ref.self(), timeout, unit);
	}

	@Override
	protected boolean isTimeout(ActorMessage<?> message, UUID timeoutId) {
		return message.tag()==Actor.TIMEOUT && timeoutId.equals(message.interaction());
	}
}
