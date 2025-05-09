/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorSystemError;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.InternalPseudoActorCell;

public abstract class ConcurrentPseudoActor implements PseudoActorRef {
	protected PseudoActor actor;

	public ConcurrentPseudoActor() {
		super();
	}

	public ConcurrentPseudoActor(String name, ActorSystem system) {
		this(name, system, false);
	}
	
	public ConcurrentPseudoActor(ActorSystem system, boolean blocking) {
		this(null, system, blocking);
	}
	
	public ConcurrentPseudoActor(String name, ActorSystem system, boolean blocking) {
		actor = new PseudoActor(name, system, blocking) {
			@Override
			public void receive(ActorMessage<?> message) {
				ConcurrentPseudoActor.this.receive(message);
			}
		};
	}
	
	@Override
	public String getName() {
		return actor.getName();
	}
	
	@Override
	public ActorId getId() {
		return actor.getId();
	}
	
	@Override
	public ActorId self() {
		return actor.getId();
	}
	
	protected void faultToleranceMethod(ActorMessage<?> message) {
		try {
			receive(message);
		}
		catch(Exception e) {
			((InternalActorSystem)actor.cell.getSystem()).getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.PSEUDO_ACTOR, actor.getId());
			/* Pseudo actors are not part of the actor system, which means 'ActorStrategyOnFailure' is not used! */
		}	
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			faultToleranceMethod(message);
			result = true;
		} 
		
		return result;
	}
	
	public abstract void receive(ActorMessage<?> message);
	
	@Override
	public boolean run() {
		boolean result = false;
		
		for (int j=0; poll(getOuterQueue()) && j<actor.getCell().getSystem().getConfig().bufferQueueSize(); j++)
			result = true;
		
		return result;
	}
	
	@Override
	public boolean runAll() {
		boolean result = false;
		
		while (poll(getOuterQueue()))
			result = true;
		
		return result;
	}
	
	@Override
	public boolean runOnce() {
		return poll(getOuterQueue());
	}
	
	@Override
	public Stream<ActorMessage<?>> stream() {
		return getOuterQueue().stream();
	}
	
	@Override
	public ActorMessage<?> await() {
		return actor.await();
	}
	
	@Override
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return actor.await(timeout, unit);
	}
	
	@Override
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return actor.await(predicate, action, timeout, unit);
	}
	
	@Override
	public void send(ActorMessage<?> message) {
		actor.send(message);
	}
	
	@Override
	public void sendViaPath(ActorMessage<?> message, String path) {
		actor.sendViaPath(message, path);
	}
	
	@Override
	public void sendViaAlias(ActorMessage<?> message, String alias) {
		actor.sendViaAlias(message, alias);
	}
	
	@Override
	public void send(ActorMessage<?> message, ActorId dest) {
		actor.send(message, dest);
	}
	
	@Override
	public <T> void tell(T value, int tag, ActorId dest) {
		actor.tell(value, tag, dest);
	}
	
	@Override
	public <T> void tell(T value, int tag, String alias) {
		actor.tell(value, tag, alias);
	}
	
	@Override
	public void forward(ActorMessage<?> message, ActorId dest) {
		actor.forward(message, dest);
	}
	
	@Override
	public void setAlias(String alias) {
		actor.setAlias(alias);
	}
	
	public Queue<ActorMessage<?>> getOuterQueue() {
		return ((InternalPseudoActorCell)actor.getCell()).getOuterQueue();
	}
	
	@Override
	public void reset() {
		((InternalPseudoActorCell)actor.getCell()).getOuterQueue().clear();
	}
}
