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
package io.actor4j.core.actors;

import java.util.Queue;
import java.util.function.Predicate;

import io.actor4j.core.EmbeddedActorCell;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public abstract class EmbeddedActor implements EmbeddedActorRef {
	protected /*quasi final*/ EmbeddedActorCell cell;
	
	protected final String name;
	
	protected Queue<ActorMessage<?>> stash; //must be initialized by hand
	
	public EmbeddedActor() {
		this(null);
	}
	
	public EmbeddedActor(String name) {
		super();
		this.name = name;
	}
	
	public EmbeddedActorCell getCell() {
		return cell;
	}

	public void setCell(EmbeddedActorCell cell) {
		this.cell = cell;
	}
	
	@Override
	public ActorRef host() {
		return cell.host();
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public ActorId getId() {
		return cell.getId();
	}
	
	@Override
	public ActorId self() {
		return cell.getId();
	}
	
	@Override
	public ActorId getParent() {
		return cell.getParent();
	}
	
	public abstract boolean receive(ActorMessage<?> message);
	
	public void become(Predicate<ActorMessage<?>> behaviour, boolean replace) {
		cell.become(behaviour, replace);
	}
	
	public void become(Predicate<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}

	public void unbecome() {
		cell.unbecome();
	}
	
	public void unbecomeAll() {
		cell.unbecomeAll();
	}
	
	public void await(final ActorId source, final Predicate<ActorMessage<?>> action, boolean replace) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.source().equals(source))
					result = action.test(message);
				return result;
			}
		}, replace);
	}
	
	public void await(final ActorId source, final Predicate<ActorMessage<?>> action) {
		await(source, action, true);
	}
	
	public void await(final int tag, final Predicate<ActorMessage<?>> action, boolean replace) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.tag()==tag)
					result = action.test(message);
				return result;
			}
		}, replace);
	}
	
	public void await(final int tag, final Predicate<ActorMessage<?>> action) {
		await(tag, action, true);
	}
	
	public void await(final ActorId source, final int tag, final Predicate<ActorMessage<?>> action, boolean replace) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.source().equals(source) && message.tag()==tag)
					result = action.test(message);
				return result;
			}
		}, replace);
	}
	
	public void await(final ActorId source, final int tag, final Predicate<ActorMessage<?>> action) {
		await(source, tag, action, true);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Predicate<ActorMessage<?>> action, boolean replace) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (predicate.test(message))
					result = action.test(message);
				return result;
			}
		}, replace);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Predicate<ActorMessage<?>> action) {
		await(predicate, action, true);
	}
	
	@Override
	public void send(ActorMessage<?> message) {
		cell.send(message);
	}
	
	@Override
	public void send(ActorMessage<?> message, ActorId dest) {
		send(message.shallowCopy(self(), dest));
	}
	
	@Override
	public <T> void tell(T value, int tag, ActorId dest) {
		send(ActorMessage.create(value, tag, self(), dest));
	}
	
	@Override
	public void forward(ActorMessage<?> message, ActorId dest) {
		send(message.shallowCopy(dest));
	}
	
	public void preStart() {
		// empty
	}
	
	public void preRestart(Exception reason) {
		cell.restart(reason);
	}
	
	public void postRestart(Exception reason) {
		cell.preStart();
	}
	
	public void postStop() {
		// empty
	}
	
	public void stop() {
		cell.stop();
	}
}
