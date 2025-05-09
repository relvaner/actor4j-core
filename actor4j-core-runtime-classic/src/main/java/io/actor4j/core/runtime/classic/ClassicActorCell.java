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
package io.actor4j.core.runtime.classic;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.BaseActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public class ClassicActorCell extends BaseActorCell implements ClassicInternalActorCell {
	protected final Queue<ActorMessage<?>> directiveQueue;
	protected final Queue<ActorMessage<?>> outerQueue;
	
	protected final AtomicBoolean isScheduled;
	
	public ClassicActorCell(InternalActorSystem system, Actor actor, ActorId id) {
		super(system, actor, id);
		
		directiveQueue = new ConcurrentLinkedQueue<>();
		outerQueue = new ConcurrentLinkedQueue<>();
		
		isScheduled = new AtomicBoolean(false);
	}

	public ClassicActorCell(InternalActorSystem system, Actor actor) {
		this(system, actor, system.idFactory().create());
	}

	@Override
	public Queue<ActorMessage<?>> directiveQueue() {
		return directiveQueue;
	}

	@Override
	public Queue<ActorMessage<?>> outerQueue() {
		return outerQueue;
	}
	
	@Override
	public AtomicBoolean isScheduled() {
		return isScheduled;
	}
	
	@Override
	public boolean aquireAsScheduled() {
		return isScheduled.compareAndSet(false, true);
	}
}
