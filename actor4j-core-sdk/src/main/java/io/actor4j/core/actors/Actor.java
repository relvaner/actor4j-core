/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.actor4j.core.ActorCell;
import io.actor4j.core.ActorSystem;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.supervisor.DefaultSupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.utils.ActorFactory;

import static io.actor4j.core.runtime.protocols.ActorProtocolTag.*;
import static io.actor4j.core.messages.ActorReservedTag.*;

public abstract class Actor implements ActorRef {
	protected /*quasi final*/ ActorCell cell;
	
	protected final String name;
	
	protected Queue<ActorMessage<?>> stash; // must be initialized by hand
	
	public static final int POISONPILL = INTERNAL_STOP;
	public static final int TERMINATED = INTERNAL_STOP_SUCCESS;
	public static final int KILL       = INTERNAL_KILL;
	
	public static final int STOP       = INTERNAL_STOP;
	public static final int RESTART    = INTERNAL_RESTART;
	
	public static final int HEALTH     = INTERNAL_HEALTH_CHECK;
	
	public static final int ACTIVATE   = INTERNAL_ACTIVATE;
	public static final int DEACTIVATE = INTERNAL_DEACTIVATE;
	
	public static final int UP    	   = RESERVED_UP; // HEALTH_CHECK_SUCCESS
	public static final int TIMEOUT    = RESERVED_TIMEOUT;
	
	/**
	 * Don't create here, new actors as child or send messages too other actors. You will 
	 * get a NullPointerException, because the variable cell is not initialized. It will 
	 * injected later by the framework. Use instead the method preStart for these reasons.
	 */
	public Actor() {
		this(null);
	}
	
	/**
	 * Don't create here, new actors as child or send messages too other actors. You will 
	 * get a NullPointerException, because the variable cell is not initialized. It will 
	 * injected later by the framework. Use instead the method preStart for these reasons.
	 */
	public Actor(String name) {
		super();
		
		this.name = name;
	}
	
	public ActorCell getCell() {
		return cell;
	}

	public void setCell(ActorCell cell) {
		this.cell = cell;
	}
	
	public ActorSystem getSystem() {
		return cell.getSystem();
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
	public String getPath() {
		return cell.getSystem().getActorPath(cell.getId());
	}
	
	@Override
	public ActorId getParent() {
		return cell.getParent();
	}
	
	@Override
	public Queue<ActorId> getChildren() {
		return cell.getChildren();
	}
	
	@Override
	public boolean isRoot() {
		return cell.isRoot();
	}
	
	@Override
	public boolean isRootInUser() {
		return cell.isRootInUser();
	}
	
	public ActorMessage<?> unstash() {
		return stash!=null ? stash.poll() : null;
	}
	
	public abstract void receive(ActorMessage<?> message);
	
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		cell.become(behaviour, replace);
	}
	
	public void become(Consumer<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	public void unbecome() {
		cell.unbecome();
	}
	
	public void unbecomeAll() {
		cell.unbecomeAll();
	}
	
	public void await(final ActorId source, final Consumer<ActorMessage<?>> action, boolean replace) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.source().equals(source)) {
					action.accept(message);
				}
			}
		}, replace);
	}
	
	public void await(final ActorId source, final Consumer<ActorMessage<?>> action) {
		await(source, action, true);
	}
	
	public void await(final int tag, final Consumer<ActorMessage<?>> action, boolean replace) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.tag()==tag) {
					action.accept(message);
				}
			}
		}, replace);
	}
	
	public void await(final int tag, final Consumer<ActorMessage<?>> action) {
		await(tag, action, true);
	}
	
	public void await(final ActorId source, final int tag, final Consumer<ActorMessage<?>> action, boolean replace) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.source().equals(source) && message.tag()==tag) {
					action.accept(message);
				}
			}
		}, replace);
	}
	
	public void await(final ActorId source, final int tag, final Consumer<ActorMessage<?>> action) {
		await(source, tag, action, true);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Consumer<ActorMessage<?>> action, boolean replace) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (predicate.test(message)) {
					action.accept(message);
				}
			}
		}, replace);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Consumer<ActorMessage<?>> action) {
		await(predicate, action, true);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final BiConsumer<ActorMessage<?>, Boolean> action, long timeout, TimeUnit unit, boolean replace) {
		ScheduledFuture<?> scheduledFuture = getSystem().globalTimer().scheduleOnce(ActorMessage.create(null, TIMEOUT, self(), null), self(), timeout, unit);
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.tag()==TIMEOUT)
					action.accept(null, true);
				else if (predicate.test(message)) {
					scheduledFuture.cancel(true);
					action.accept(message, false);
				}
			}
		}, replace);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final BiConsumer<ActorMessage<?>, Boolean> action, long timeout, TimeUnit unit) {
		await(predicate, action, timeout, unit, true);
	}
	
	@Override
	public void send(ActorMessage<?> message) {
		cell.send(message);
	}
	
	@Override
	public void sendViaPath(ActorMessage<?> message, String path) {
		ActorId dest = cell.getSystem().getActorFromPath(path);
		if (dest!=null)
			send(message, dest);
	}
	
	@Override
	public void sendViaAlias(ActorMessage<?> message, String alias) {
		cell.send(message, alias);
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
	public <T> void tell(T value, int tag, ActorId dest, String domain) {
		send(ActorMessage.create(value, tag, self(), dest, domain));
	}
	
	@Override
	public <T> void tell(T value, int tag, ActorId dest, UUID interaction) {
		send(ActorMessage.create(value, tag, self(), dest, interaction));
	}
	
	@Override
	public <T> void tell(T value, int tag, ActorId dest, UUID interaction, String protocol) {
		send(ActorMessage.create(value, tag, self(), dest, interaction, protocol));
	}
	
	@Override
	public <T> void tell(T value, int tag, ActorId dest, UUID interaction, String protocol, String domain) {
		send(ActorMessage.create(value, tag, self(), dest, interaction, protocol, domain));
	}
	
	@Override
	public <T> void tell(T value, int tag, String alias) {
		sendViaAlias(ActorMessage.create(value, tag, self(), null), alias);
	}
	
	@Override
	public <T> void tell(T value, int tag, String alias, UUID interaction) {
		sendViaAlias(ActorMessage.create(value, tag, self(), null, interaction), alias);
	}
	
	@Override
	public <T> void tell(T value, int tag, String alias, UUID interaction, String protocol) {
		sendViaAlias(ActorMessage.create(value, tag, self(), null, interaction, protocol), alias);
	}
	
	@Override
	public <T> void tell(T value, int tag, String alias, UUID interaction, String protocol, String domain) {
		sendViaAlias(ActorMessage.create(value, tag, self(), null, interaction, protocol, domain), alias);
	}
	
	@Override
	public void forward(ActorMessage<?> message, ActorId dest) {
		send(message.shallowCopy(dest));
	}
	
	@Override
	public void forward(ActorMessage<?> message, String alias) {
		sendViaAlias(message, alias);
	}
	
	@Override
	public void priority(ActorMessage<?> message) {
		cell.priority(message);
	}
	
	@Override
	public void priority(ActorMessage<?> message, ActorId dest) {
		priority(message.shallowCopy(self(), dest));
	}
	
	@Override
	public <T> void priority(T value, int tag, ActorId dest) {
		priority(ActorMessage.create(value, tag, self(), dest));
	}
	
	public void unhandled(ActorMessage<?> message) {
		cell.unhandled(message);
	}
	
	public void setAlias(String alias) {
		if (alias!=null && !alias.isEmpty())
			cell.getSystem().setAlias(self(), alias);
	}
	
	public void expose() {
		cell.expose();
	}
	
	public ActorId addChild(ActorFactory factory) {
		return cell.addChild(factory);
	}
	
	public List<ActorId> addChild(ActorFactory factory, int instances) {
		return cell.addChild(factory, instances);
	}
	
	public SupervisorStrategy supervisorStrategy() {
		return new DefaultSupervisorStrategy(getSystem().getConfig().maxRetries(), getSystem().getConfig().withinTimeRange());
	}
	
	/**
	 * Initialize here, your actor code. Create new actors as child or send too other actors messages, 
	 * before the first message for this actor could be processed.
	 */
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
	
	@Override
	public void watch(ActorId dest) {
		cell.watch(dest);
	}
	
	@Override
	public void unwatch(ActorId dest) {
		cell.unwatch(dest);
	}
}
