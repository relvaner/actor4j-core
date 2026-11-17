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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.actor4j.core.messages.ActorMessage;

// current behaviour and the evaluation of the return value, independent of the kind of actor.
// Subclasses: ActorBehaviourContext (Actor), EmbeddedActorBehaviourContext (EmbeddedActor)
public abstract class AbstractBehaviourContext {
	protected final Supplier<ActorBehaviour> initial;
	protected ActorBehaviour current;
	protected final Set<ScheduledFuture<?>> timeouts; // pending timeouts of await, cancelled on stop

	public AbstractBehaviourContext(Supplier<ActorBehaviour> initial) {
		super();
		this.initial = initial;
		this.timeouts = new HashSet<>();
	}

	// created on first use: the behaviour methods of the actor may use its fields
	public ActorBehaviour current() {
		if (current==null)
			current = initial.get();
		return current;
	}

	// name of the current behaviour, e.g. for actor4j-verification (ActorVerificationState)
	public String state() {
		return current().name();
	}

	// evaluates the return value of the current behaviour, the result is same(), unhandled() or stopped()
	protected ActorBehaviour next(ActorMessage<?> message) {
		ActorBehaviour next = current().handle(message);

		if (next==null || next==ActorBehaviour.same())
			return ActorBehaviour.same();
		else if (next==ActorBehaviour.unhandled())
			return ActorBehaviour.unhandled();
		else if (next==ActorBehaviour.stopped()) {
			cancelTimeouts();
			return ActorBehaviour.stopped();
		}
		else if (next==ActorBehaviour.initial())
			current = initial.get();
		else
			current = next;

		return ActorBehaviour.same();
	}

	protected void cancelTimeouts() {
		for (ScheduledFuture<?> timeout : timeouts)
			timeout.cancel(true);
		timeouts.clear();
	}

	// sends a timeout message to the actor after the delay, recognized by isTimeout
	protected abstract ScheduledFuture<?> scheduleTimeout(UUID timeoutId, long timeout, TimeUnit unit);

	protected abstract boolean isTimeout(ActorMessage<?> message, UUID timeoutId);

	// waits for a message matching the predicate, the action returns the behaviour after the message.
	// Other messages go to otherwise (default: the behaviour current when await was called) and waiting goes on,
	// except otherwise returns stopped (waiting ends, actor stops) or unhandled (passed on).
	public ActorBehaviour await(Predicate<ActorMessage<?>> predicate, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action) {
		return await(predicate, otherwise, action, 0, null, null);
	}

	public ActorBehaviour await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, ActorBehaviour> action) {
		return await(predicate, null, action);
	}

	public ActorBehaviour await(int tag, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action) {
		return await((message) -> message.tag()==tag, otherwise, action);
	}

	public ActorBehaviour await(int tag, Function<ActorMessage<?>, ActorBehaviour> action) {
		return await(tag, null, action);
	}

	// with timeout: onTimeout returns the behaviour if no matching message arrives in time
	public ActorBehaviour await(Predicate<ActorMessage<?>> predicate, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action,
			long timeout, TimeUnit unit, Supplier<ActorBehaviour> onTimeout) {
		ActorBehaviour others = otherwise!=null ? otherwise : (current!=null ? current : (message) -> ActorBehaviour.unhandled());
		UUID timeoutId = UUID.randomUUID();
		ScheduledFuture<?> scheduledFuture = unit!=null ? scheduleTimeout(timeoutId, timeout, unit) : null;
		if (scheduledFuture!=null)
			timeouts.add(scheduledFuture);

		return (message) -> {
			if (scheduledFuture!=null && isTimeout(message, timeoutId)) {
				timeouts.remove(scheduledFuture);
				return onTimeout.get();
			}
			if (predicate.test(message)) {
				if (scheduledFuture!=null) {
					scheduledFuture.cancel(true);
					timeouts.remove(scheduledFuture);
				}
				return action.apply(message);
			}

			ActorBehaviour next = others.handle(message);
			if (next==ActorBehaviour.stopped()) {
				if (scheduledFuture!=null) {
					scheduledFuture.cancel(true);
					timeouts.remove(scheduledFuture);
				}
				return ActorBehaviour.stopped();
			}
			return next==ActorBehaviour.unhandled() ? ActorBehaviour.unhandled() : ActorBehaviour.same();
		};
	}

	public ActorBehaviour await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, ActorBehaviour> action,
			long timeout, TimeUnit unit, Supplier<ActorBehaviour> onTimeout) {
		return await(predicate, null, action, timeout, unit, onTimeout);
	}

	public ActorBehaviour await(int tag, ActorBehaviour otherwise, Function<ActorMessage<?>, ActorBehaviour> action,
			long timeout, TimeUnit unit, Supplier<ActorBehaviour> onTimeout) {
		return await((message) -> message.tag()==tag, otherwise, action, timeout, unit, onTimeout);
	}

	public ActorBehaviour await(int tag, Function<ActorMessage<?>, ActorBehaviour> action,
			long timeout, TimeUnit unit, Supplier<ActorBehaviour> onTimeout) {
		return await(tag, null, action, timeout, unit, onTimeout);
	}
}
