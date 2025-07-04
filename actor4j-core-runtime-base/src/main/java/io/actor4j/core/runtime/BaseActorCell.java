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

package io.actor4j.core.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.actor4j.core.ActorCell;
import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.PersistenceId;
import io.actor4j.core.actors.PersistentActor;
import io.actor4j.core.exceptions.ActorInitializationException;
import io.actor4j.core.exceptions.ActorKilledException;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.json.JsonObject;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.persistence.ActorPersistenceDTO;
import io.actor4j.core.runtime.annotations.concurrent.ThreadLocalAccess;
import io.actor4j.core.runtime.annotations.concurrent.ThreadSafeAccess;
import io.actor4j.core.runtime.di.FactoryInjector;
import io.actor4j.core.runtime.persistence.actor.PersistenceServiceActor;
import io.actor4j.core.runtime.protocols.RecoverProtocol;
import io.actor4j.core.runtime.protocols.RestartProtocol;
import io.actor4j.core.runtime.protocols.StopProtocol;
import io.actor4j.core.runtime.protocols.StopUserSpaceProtocol;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.utils.ActorFactory;

import static io.actor4j.core.actors.Actor.*;
import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.runtime.protocols.ActorProtocolTag.*;
import static io.actor4j.core.utils.ActorUtils.*;

public class BaseActorCell implements InternalActorCell {
	static record PersistenceTuple(Consumer<Object> onSuccess, Consumer<Exception> onFailure, List<Object> objects) {
	}

	protected final InternalActorSystem system;
	@ThreadSafeAccess(reason = "Injected along with the creation of the actor cell or overwritten at restart")
	protected Actor actor;
	@ThreadSafeAccess
	protected final AtomicReference<UUID> globalId; // Optionally generated when exposed!
	@ThreadSafeAccess(reason = "Injected along with the creation of the actor cell") @ThreadLocalAccess
	protected /*quasi final*/ FactoryInjector<?> factory;
	@ThreadSafeAccess(reason = "Initialized at the registration of the actor cell")
	protected long threadId;
	@ThreadSafeAccess(reason = "Initialized along with the creation o the actor cell")
	protected /*quasi final*/ ActorId parent;
	@ThreadSafeAccess
	protected final Queue<ActorId> children;
	@ThreadSafeAccess(reason = "Can be overwritten at runtime")
	protected volatile ActorId redirect;
	@ThreadSafeAccess(reason = "...")
	protected final AtomicBoolean active;
	@ThreadLocalAccess
	protected final Deque<Consumer<ActorMessage<?>>> behaviourStack;
	@ThreadSafeAccess
	protected final Queue<ActorId> deathWatcher;
	@ThreadLocalAccess
	protected boolean activeDirectiveBehaviour;
	@ThreadLocalAccess
	protected final Queue<PersistenceTuple> persistenceTuples;
	
	@ThreadSafeAccess
	protected final AtomicLong requestRate;
	@ThreadSafeAccess
	protected final Queue<Long> processingTimeSamples;
	
	@ThreadLocalAccess(reason = "Can be overwritten at runtime")
	protected SupervisorStrategy parentSupervisorStrategy;
			
	public BaseActorCell(InternalActorSystem system, Actor actor) {
		super();
		
		this.system = system;
		this.actor  = actor;

		this.globalId = new AtomicReference<UUID>(null);
		
		threadId = -1;
		
		children = system.createLockFreeLinkedQueue();
		
		active = new AtomicBoolean(true);
		
		behaviourStack = new ArrayDeque<>();
		
		deathWatcher =  system.createLockFreeLinkedQueue();
		
		persistenceTuples = new LinkedList<>();
		
		requestRate = new AtomicLong(0);
		processingTimeSamples = system.createLockFreeLinkedQueue();
	}
	
	public void cleanUp() {
//		actor = null;
		globalId.set(null);
		factory = null;

		threadId = -1;

//		parent = null;
		redirect = null;
		children.clear();

		active.set(true);

		behaviourStack.clear();

		deathWatcher.clear();

		persistenceTuples.clear();

		requestRate.set(0);
		processingTimeSamples.clear();

		parentSupervisorStrategy = null;
	}

	@Override
	public ActorSystem getSystem() {
		return system;
	}
	
	@Override
	public Actor getActor() {
		return actor;
	}
	
	@Override
	public void setActor(Actor actor) {
		this.actor = actor;
	}

	@Override
	public FactoryInjector<?> getFactory() {
		return factory;
	}

	@Override
	public void setFactory(FactoryInjector<?> factory) {
		this.factory = factory;
	}

	@Override
	public int getType() {
		return ActorCell.DEFAULT_ACTOR_CELL;
	}
	
	@Override
	public boolean isPod() {
		return false;
	}
	
	@Override
	public ActorId localId() {
		return this;
	}
	
	@Override
	public UUID globalId() {
		return globalId.get();
	}
	
	@Override
	public void expose() {
		UUID uuid = globalId.get();
		if (uuid==null) {
			uuid = persistenceId()!=null ? persistenceId() : UUID.randomUUID();
			globalId.set(uuid);
		}
		system.getExposedCells().put(uuid, this);
	}
	
	@Override
	public ActorId redirectId() {
		return redirect;
	}

	@Override
	public ActorId getId() {
		return this;
	}
	
	@Override
	public ActorId getParent() {
		return parent;
	}
	
	@Override
	public void setParent(ActorId parent) {
		this.parent = parent;
	}

	@Override
	public Queue<ActorId> getChildren() {
		return children;
	}
	
	@Override
	public ActorId getRedirect() {
		return redirect;
	}

	@Override
	public void setRedirect(ActorId redirect) {
		this.redirect = redirect;
	}
	
	@Override
	public long getThreadId() {
		return threadId;
	}

	@Override
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	@Override
	public boolean isActive() {
		return active.get();
	}

	@Override
	public void setActive(boolean active) {
		this.active.set(active);
	}
	
	@Override
	public Queue<ActorId> getDeathWatcher() {
		return deathWatcher;
	}

	@Override
	public void setActiveDirectiveBehaviour(boolean activeDirectiveBehaviour) {
		this.activeDirectiveBehaviour = activeDirectiveBehaviour;
	}

	@Override
	public boolean isRoot() {
		return (parent==null);
	}
	
	public boolean hasUserId() {
		return getId()==system.USER_ID();
	}
	
	@Override
	public boolean isRootInUser() {
		return parent!=null ? (parent==system.USER_ID()) : false;
	}
	
	@Override
	public boolean isRootInSystem() {
		return parent!=null ? (parent==system.SYSTEM_ID()) : false;
	}
	
	protected boolean processedDirective(ActorMessage<?> message) {
		boolean result = false;
		
		if (isDirective(message) && !activeDirectiveBehaviour) {
			result = true;
			if (message.tag()==INTERNAL_RESTART || message.tag()==INTERNAL_STOP)
				activeDirectiveBehaviour = true;
				
			if (message.tag()==INTERNAL_RESTART) {
				if (message.value() instanceof Exception)
					preRestart((Exception)message.value());
				else
					preRestart(null);
			}
			else if (message.tag()==INTERNAL_STOP)
				stop();
			else if (message.tag()==INTERNAL_KILL) 
				throw new ActorKilledException();
			else if (message.tag()==INTERNAL_HEALTH_CHECK)
				send(ActorMessage.create(null, UP, getId(), message.source()));
			else if (message.tag()==INTERNAL_ACTIVATE)
				active.set(true);
			else if (message.tag()==INTERNAL_DEACTIVATE)
				active.set(false);
			else if (message.tag()==INTERNAL_RECOVER)
				RecoverProtocol.apply(this);
			else if (message.tag()==INTERNAL_PERSISTENCE_RECOVER)
				recover(message);
			else if (message.tag()==INTERNAL_PERSISTENCE_SUCCESS) {
				PersistenceTuple tuple = persistenceTuples.poll();
				if (tuple.onSuccess()!=null)
					for (int i=0; i<tuple.objects().size(); i++)
						tuple.onSuccess().accept(tuple.objects().get(i));
			}
			else if (message.tag()==INTERNAL_PERSISTENCE_FAILURE) {
				PersistenceTuple tuple = persistenceTuples.poll();
				if (tuple.onFailure()!=null)
					tuple.onFailure().accept((Exception)message.value());
			}
			else if (message.tag()==INTERNAL_STOP_USER_SPACE && hasUserId()) {
				StopUserSpaceProtocol.apply(this);
			}
			else
				result = false;
		}
		
		return result;
	}
	
	@Override
	public void internal_receive(ActorMessage<?> message) {
		if (!processedDirective(message) && active.get()) {
			if (behaviourStack.isEmpty())
				actor.receive(message);
			else {
				Consumer<ActorMessage<?>> behaviour = behaviourStack.peek();
				behaviour.accept(message);
			}
		}
	}
	
	@Override
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		if (replace && !behaviourStack.isEmpty())
			behaviourStack.pop();
		behaviourStack.push(behaviour);
	}
	
	@Override
	public void become(Consumer<ActorMessage<?>> behaviour) {
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
	public void send(ActorMessage<?> message) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().post(message, getId());
		else
			system.getBufferQueue().offer(message.copy());
	}

	@Override
	public void send(ActorMessage<?> message, ActorId dest) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().post(message.shallowCopy(dest), getId());
		else
			system.getBufferQueue().offer(message.copy(dest));
	}
	
	@Override
	public void send(ActorMessage<?> message, String alias) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().post(message, getId(), alias);
		else {
			if (alias!=null) {
				List<ActorId> destinations = system.getActorsFromAlias(alias);

				ActorId dest = null;
				if (!destinations.isEmpty()) {
					if (destinations.size()==1)
						dest = destinations.get(0);
					else
						dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
				}
				dest = (dest!=null) ? dest : system.ALIAS_ID();
				system.getBufferQueue().offer(message.copy(dest));
			}
		}
	}
	
	@Override
	public void send(ActorMessage<?> message, UUID globalId) {
		ActorId dest = system.getExposedCells().get(globalId);
		if (dest!=null)
			send(message, dest);
	}
	
	@Override
	public void unsafe_send(ActorMessage<?> message) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().unsafe_post(message, getId());
		else
			system.getBufferQueue().offer(message.copy());
	}
	
	@Override
	public void unsafe_send(ActorMessage<?> message, String alias) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().unsafe_post(message, getId(), alias);
		else {
			if (alias!=null) {
				List<ActorId> destinations = system.getActorsFromAlias(alias);

				ActorId dest = null;
				if (!destinations.isEmpty()) {
					if (destinations.size()==1)
						dest = destinations.get(0);
					else
						dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
				}
				dest = (dest!=null) ? dest : system.ALIAS_ID();
				system.getBufferQueue().offer(message.copy(dest));
			}
		}
	}
	
	@Override
	public void priority(ActorMessage<?> message) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().postPriority(message);
	}
	
	@Override
	public void unhandled(ActorMessage<?> message) {
		if (system.getConfig().debugUnhandled()) {
			Actor sourceActor = ((InternalActorCell)message.source()).getActor();
			if (sourceActor!=null)
				systemLogger().log(WARN,
					String.format("[UNHANDLED] actor (%s) - Unhandled message (%s) from source (%s)",
						actorLabel(actor), message.toString(), actorLabel(sourceActor)
					));
			else
				systemLogger().log(WARN,
					String.format("[UNHANDLED] actor (%s) - Unhandled message (%s) from unavaible source (???)",
						actorLabel(actor), message.toString()
					));
		}
	}
	
	@Override
	public ActorId internal_addChild(InternalActorCell cell) {
		cell.setParent(getId());
		children.add(cell.getId());
		system.internal_addCell(cell);
		
		return cell.getId();
	}
	
	@Override
	public ActorId addChild(ActorFactory factory) {
		InternalActorCell cell = system.generateCell(factory.create());
		cell.setFactory(factory);
		
		return internal_addChild(cell);
	}
	
	@Override
	public List<ActorId> addChild(ActorFactory factory, int instances) {
		List<ActorId> result = new ArrayList<>(instances);
		
		for (int i=0; i<instances; i++)
			result.add(addChild(factory));
		
		return result;
	}
	
	@Override
	public SupervisorStrategy supervisorStrategy() {
		return actor.supervisorStrategy();
	}
	
	@Override
	public void preStart() {
		RecoverProtocol.apply(this);
		try {
			actor.preStart();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ActorInitializationException();
		}
	}
	
	@Override
	public void preRestart(Exception reason) {
		try {
			actor.preRestart(reason);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void postRestart(Exception reason) {
		try {
			actor.postRestart(reason);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void postStop() {
		try {
			actor.postStop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void restart(Exception reason) {
		RestartProtocol.apply(this, reason);
	}
	
	@Override
	public void stop() {
		StopProtocol.apply(this);
	}
	
	@Override
	public void internal_stop() {
		if (parent!=null)
			((InternalActorCell)parent).getChildren().remove(getId());
		/*if (!(actor instanceof ResourceActor) && !(actor instanceof PseudoActor)) @See: ActorMessageDispatcher */
			system.getMessageDispatcher().unregisterCell(this);
		system.removeActor(getId());

		Iterator<ActorId> iterator = deathWatcher.iterator();
		while (iterator.hasNext()) {
			ActorId dest = iterator.next();
			system.sendAsDirective(ActorMessage.create(null, INTERNAL_STOP_SUCCESS, getId(), dest));
			iterator.remove();
		}
		
		if (parent!=null)
			system.sendAsDirective(ActorMessage.create(null, INTERNAL_STOP_SUCCESS, getId(), parent));
		
		cleanUp();
	}
	
	@Override
	public void watch(ActorId dest) {
		InternalActorCell cell = ((InternalActorCell)dest);
		if (cell!=null)
			cell.getDeathWatcher().add(getId());
	}
	
	@Override
	public void unwatch(ActorId dest) {
		InternalActorCell cell = ((InternalActorCell)dest);
		if (cell!=null)
			cell.getDeathWatcher().remove(getId());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, E... events) {	
		if (system.getConfig().persistenceMode() && events!=null) {
			List<ActorPersistenceDTO<Object>> list = new ArrayList<>(events.length);
			for (int i=0; i<events.length; i++)
				list.add(new ActorPersistenceDTO<>(events[i], persistenceId(), System.currentTimeMillis(), 0));
			PersistenceTuple tuple = new PersistenceTuple((Consumer<Object>)onSuccess, onFailure, Arrays.asList(events));
			system.getMessageDispatcher().postPersistence(ActorMessage.create(new ImmutableList<>(list), PersistenceServiceActor.PERSIST_EVENTS, getId(), null));
			persistenceTuples.offer(tuple);
			
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S> void saveSnapshot(Consumer<S> onSuccess, Consumer<Exception> onFailure, S state) {
		if (system.getConfig().persistenceMode() && state!=null) {
			List<ActorPersistenceDTO<Object>> list = new ArrayList<>();
			list.add(new ActorPersistenceDTO<>(state, persistenceId(), System.currentTimeMillis(), 0));
			PersistenceTuple tuple = new PersistenceTuple((Consumer<Object>)onSuccess, onFailure, Arrays.asList(state));
			system.getMessageDispatcher().postPersistence(ActorMessage.create(list.get(0), PersistenceServiceActor.PERSIST_STATE, getId(), null));
			persistenceTuples.offer(tuple);
		}
	}
	
	public void recover(ActorMessage<?> message) {
		if (system.getConfig().persistenceMode() && actor instanceof PersistentActor && message.value() instanceof JsonObject) {
			((PersistentActor<?, ?>)actor).recover(message.valueAsJsonObject());
			active.set(true);
		}
	}
	
	public UUID persistenceId() {
		UUID result = null;
		if (actor instanceof PersistenceId)
			result = ((PersistenceId)actor).persistenceId();
		
		return result;
	}

	@Override
	public AtomicLong getRequestRate() {
		return requestRate;
	}

	@Override
	public Queue<Long> getProcessingTimeSamples() {
		return processingTimeSamples;
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
