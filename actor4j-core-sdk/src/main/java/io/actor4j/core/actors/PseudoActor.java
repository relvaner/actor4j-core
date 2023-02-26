/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.exceptions.ActorInitializationException;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.InternalPseudoActorCell;
import io.actor4j.core.utils.ActorFactory;

public abstract class PseudoActor extends Actor {
	public PseudoActor(ActorSystem system, boolean blocking) {
		this(null, system, blocking);
	}
	
	public PseudoActor(String name, ActorSystem system, boolean blocking) {
		super(name);
		
		InternalPseudoActorCell cell = ((InternalActorSystem)system).getPseudoActorCellFactory().apply((InternalActorSystem)system, this, blocking);
		setCell(cell);
		cell.pseudo_addCell(cell);
		/* preStart */
		try {
			preStart();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ActorInitializationException();
		}
	}
	
	@Deprecated
	@Override
	public UUID addChild(ActorFactory factory) {
		return null;
	}
	
	@Deprecated
	@Override
	public List<UUID> addChild(ActorFactory factory, int instances) {
		return null;
	}

	public boolean run() {
		return ((InternalPseudoActorCell)cell).run();
	}
	
	public boolean runOnce() {
		return ((InternalPseudoActorCell)cell).runOnce();
	}
	
	public Stream<ActorMessage<?>> stream() {
		return ((InternalPseudoActorCell)cell).stream();
	}
	
	public ActorMessage<?> await() {
		return ((InternalPseudoActorCell)cell).await();
	}
	
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((InternalPseudoActorCell)cell).await(timeout, unit);
	}
	
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((InternalPseudoActorCell)cell).await(predicate, action, timeout, unit);
	}
	
	public void reset() {
		((InternalPseudoActorCell)cell).reset();
	}
}
