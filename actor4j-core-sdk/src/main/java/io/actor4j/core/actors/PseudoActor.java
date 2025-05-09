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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.InternalPseudoActorCell;
import io.actor4j.core.utils.ActorFactory;

public abstract class PseudoActor extends Actor implements PseudoActorRef {
	public PseudoActor(ActorSystem system, boolean blocking) {
		this(null, system, blocking);
	}
	
	public PseudoActor(String name, ActorSystem system, boolean blocking) {
		super(name);
		
		InternalPseudoActorCell cell = ((InternalActorSystem)system).getPseudoActorCellFactory().apply((InternalActorSystem)system, this, blocking);
		setCell(cell);
		cell.pseudo_addCell(cell);
		/* preStart */
		cell.preStart();
	}
	
	@Deprecated
	@Override
	public ActorId addChild(ActorFactory factory) {
		return null;
	}
	
	@Deprecated
	@Override
	public List<ActorId> addChild(ActorFactory factory, int instances) {
		return null;
	}

	@Override
	public boolean run() {
		return ((InternalPseudoActorCell)cell).run();
	}
	
	@Override
	public boolean runAll() {
		return ((InternalPseudoActorCell)cell).runAll();
	}
	
	@Override
	public boolean runOnce() {
		return ((InternalPseudoActorCell)cell).runOnce();
	}
	
	@Override
	public Stream<ActorMessage<?>> stream() {
		return ((InternalPseudoActorCell)cell).stream();
	}
	
	@Override
	public ActorMessage<?> await() {
		return ((InternalPseudoActorCell)cell).await();
	}
	
	@Override
	public ActorMessage<?> await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((InternalPseudoActorCell)cell).await(timeout, unit);
	}
	
	@Override
	public <T> T await(Predicate<ActorMessage<?>> predicate, Function<ActorMessage<?>, T> action, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return ((InternalPseudoActorCell)cell).await(predicate, action, timeout, unit);
	}
	
	@Override
	public void reset() {
		((InternalPseudoActorCell)cell).reset();
	}
}
