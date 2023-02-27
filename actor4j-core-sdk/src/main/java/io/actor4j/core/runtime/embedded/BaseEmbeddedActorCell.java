/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.embedded;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.function.Predicate;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.exceptions.ActorInitializationException;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.embedded.protocol.RestartProtocol;
import io.actor4j.core.runtime.embedded.protocol.StopProtocol;
import io.actor4j.core.supervisor.SupervisorStrategy;

public class BaseEmbeddedActorCell implements InternalEmbeddedActorCell {
	protected final ActorRef host;
	protected /*quasi final*/ EmbeddedActor actor;
	
	protected final UUID id;
	
	protected boolean active;
	
	protected final Deque<Predicate<ActorMessage<?>>> behaviourStack;
	
	protected final RestartProtocol restartProtocol;
	protected final StopProtocol stopProtocol;
	
	protected SupervisorStrategy parentSupervisorStrategy;
	
	public BaseEmbeddedActorCell(ActorRef host, EmbeddedActor actor) {
		this(host, actor, UUID.randomUUID());
	}
	
	public BaseEmbeddedActorCell(ActorRef host, EmbeddedActor actor, UUID id) {
		super();
		this.host = host;
		this.actor = actor;
		this.id = id;
		active = true;
		behaviourStack = new ArrayDeque<>();
		restartProtocol = new RestartProtocol(this);
		stopProtocol = new StopProtocol(this);
	}

	@Override
	public EmbeddedActor getActor() {
		return actor;
	}

	@Override
	public void setActor(EmbeddedActor actor) {
		this.actor = actor;
	}
	
	@Override
	public ActorRef host() {
		return host;
	}
	
	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public UUID getParent() {
		return host.getId();
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean embedded(ActorMessage<?> message) {
		boolean result = false;
		
		if (active) {
			Predicate<ActorMessage<?>> behaviour = behaviourStack.peek();
			if (behaviour==null)
				result = actor.receive(message);
			else
				result = behaviour.test(message);	
		}
		
		return result;
	}
	
	public <T> boolean embedded(T value, int tag, UUID dest) {
		return embedded(ActorMessage.create(value, tag, getParent(), dest));
	}
	
	@Override
	public void become(Predicate<ActorMessage<?>> behaviour, boolean replace) {
		if (replace && !behaviourStack.isEmpty())
			behaviourStack.pop();
		behaviourStack.push(behaviour);
	}

	@Override
	public void become(Predicate<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	@Override
	public void unbecome() {
		behaviourStack.pop();
	}
	
	@Override
	public void unbecomeAll() {
		behaviourStack.clear();
	}
	
	@Override
	public void fireActiveBehaviour(ActorMessage<?> message, Predicate<Integer> condition) {
		if (condition.test(behaviourStack.size()))
			behaviourStack.peek().test(message);
	}
	
	@Override
	public void send(ActorMessage<?> message) {
		if (host instanceof EmbeddedHostActor h)
			h.underlyingImpl().sendWithinHost(message);
	}
	
	@Override
	public void unsafe_send(ActorMessage<?> message) {
		if (host instanceof EmbeddedHostActor h)
			h.underlyingImpl().sendUnsafeWithinHost(message);
	}
	
	@Override
	public void preStart() {
		try {
			actor.preStart();
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new ActorInitializationException();
		}
	}
	
	@Override
	public void preRestart(Exception reason) {
		try {
			actor.preRestart(reason);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void postRestart(Exception reason) {
		try {
			actor.postRestart(reason);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void postStop() {
		try {
			actor.postStop();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void restart(Exception reason) {
		restartProtocol.apply(reason);
	}

	@Override
	public void stop() {
		stopProtocol.apply();
	}

	@Override
	public SupervisorStrategy getParentSupervisorStrategy() {
		return parentSupervisorStrategy;
	}

	@Override
	public void setParentSupervisorStrategy(SupervisorStrategy parentSupervisorStrategy) {
		this.parentSupervisorStrategy = parentSupervisorStrategy;
	}
}
