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
import io.actor4j.core.actors.EmbeddedActorRef;
import io.actor4j.core.messages.ActorMessage;

// current behaviour of an embedded actor, receive returns true when the embedded actor is done (stopped()):
// 	protected final EmbeddedActorBehaviourContext behaviours = new EmbeddedActorBehaviourContext(this, this::initial);
// 	public boolean receive(ActorMessage<?> message) { return behaviours.receive(message); }
// The host routes by interaction, so the timeout goes to the host with the globalId of the embedded actor as
// interaction, the host has to pass Actor.TIMEOUT on to the embedded actor (e.g. EmbeddedHandlerActor.handle).
public class EmbeddedActorBehaviourContext extends AbstractBehaviourContext {
	protected final EmbeddedActorRef ref;

	public EmbeddedActorBehaviourContext(EmbeddedActorRef ref, Supplier<ActorBehaviour> initial) {
		super(initial);
		this.ref = ref;
	}

	// true: done, the host removes the embedded actor
	public boolean receive(ActorMessage<?> message) {
		ActorBehaviour result = next(message);

		if (result==ActorBehaviour.unhandled() && ref.host() instanceof Actor host)
			host.unhandled(message);

		return result==ActorBehaviour.stopped();
	}

	@Override
	protected ScheduledFuture<?> scheduleTimeout(UUID timeoutId, long timeout, TimeUnit unit) {
		ActorRef host = ref.host();
		return host.getSystem().globalTimer().scheduleOnce(
			ActorMessage.create(timeoutId, Actor.TIMEOUT, host.self(), host.self(), ref.self().globalId()), host.self(), timeout, unit);
	}

	@Override
	protected boolean isTimeout(ActorMessage<?> message, UUID timeoutId) {
		return message.tag()==Actor.TIMEOUT && timeoutId.equals(message.value());
	}
}
