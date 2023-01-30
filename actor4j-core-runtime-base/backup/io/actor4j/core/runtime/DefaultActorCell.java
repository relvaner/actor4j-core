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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.PersistenceId;
import io.actor4j.core.actors.PersistentActor;
import io.actor4j.core.exceptions.ActorKilledException;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.persistence.ActorPersistenceDTO;
import io.actor4j.core.runtime.persistence.actor.PersistenceServiceActor;
import io.actor4j.core.runtime.protocols.RecoverProtocol;
import io.actor4j.core.runtime.protocols.RestartProtocol;
import io.actor4j.core.runtime.protocols.StopProtocol;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.utils.ActorFactory;

import static io.actor4j.core.actors.Actor.*;
import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.runtime.protocols.ActorProtocolTag.*;
import static io.actor4j.core.utils.ActorUtils.*;

public class DefaultActorCell implements InternalActorCell {
	static record PersistenceTuple(Consumer<Object> onSuccess, Consumer<Exception> onFailure, List<Object> objects) {
	}
	
	protected final InternalActorSystem system;
	protected /*quasi final*/ Actor actor;
	
	protected final UUID id;
	
	protected UUID parent;
	protected final Queue<UUID> children;
	
	protected final AtomicBoolean active;
	
	protected final Deque<Consumer<ActorMessage<?>>> behaviourStack;
	
	protected final RestartProtocol restartProtocol;
	protected final StopProtocol stopProtocol;
	protected final RecoverProtocol recoverProtocol;
	
	protected final Queue<UUID> deathWatcher;
	
	protected final Function<ActorMessage<?>, Boolean> processedDirective;
	protected boolean activeDirectiveBehaviour;
	
	protected final Queue<PersistenceTuple> persistenceTuples;
	
	protected final AtomicLong requestRate;
	protected final Queue<Long> processingTimeStatistics;
	
	protected SupervisorStrategy parentSupervisorStrategy;
	
	public DefaultActorCell(InternalActorSystem system, Actor actor) {
		this(system, actor, UUID.randomUUID());
	}
			
	public DefaultActorCell(InternalActorSystem system, Actor actor, UUID id) {
		super();
		
		this.system = system;
		this.actor  = actor;
		
		UUID persistenceId = persistenceId();
		this.id = (persistenceId!=null)? persistenceId : id;
		
		children = new ConcurrentLinkedQueue<>();
		
		active = new AtomicBoolean(true);
		
		behaviourStack = new ArrayDeque<>();
		
		restartProtocol = new RestartProtocol(this);
		stopProtocol = new StopProtocol(this);
		recoverProtocol = new RecoverProtocol(this);
		
		deathWatcher =  new ConcurrentLinkedQueue<>();
		
		processedDirective = new Function<ActorMessage<?>, Boolean>() {
			@Override
			public Boolean apply(ActorMessage<?> message) {
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
						send(ActorMessage.create(null, UP, id, message.source()));
					else if (message.tag()==INTERNAL_ACTIVATE)
						active.set(true);
					else if (message.tag()==INTERNAL_DEACTIVATE)
						active.set(false);
					else if (message.tag()==INTERNAL_RECOVER)
						recoverProtocol.apply();
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
					else
						result = false;
				}
				
				return result;
			}	
		};
		
		persistenceTuples = new LinkedList<>();
		
		requestRate = new AtomicLong(0);
		processingTimeStatistics = new ConcurrentLinkedQueue<>();
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
	public UUID getId() {
		return id;
	}
	
	@Override
	public UUID getParent() {
		return parent;
	}
	
	@Override
	public void setParent(UUID parent) {
		this.parent = parent;
	}

	@Override
	public Queue<UUID> getChildren() {
		return children;
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
	public Queue<UUID> getDeathWatcher() {
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
	
	@Override
	public boolean isRootInUser() {
		return parent!=null ? (parent.equals(system.USER_ID())) : false;
	}
	
	@Override
	public boolean isRootInSystem() {
		return parent!=null ? (parent.equals(system.SYSTEM_ID())) : false;
	}
	
	@Override
	public void internal_receive(ActorMessage<?> message) {
		if (!processedDirective.apply(message) && active.get()) {
			Consumer<ActorMessage<?>> behaviour = behaviourStack.peek();
			if (behaviour==null)
				actor.receive(message);
			else
				behaviour.accept(message);	
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
			system.getMessageDispatcher().post(message, id);
		else
			system.getBufferQueue().offer(message.copy());
	}
	
	@Override
	public void send(ActorMessage<?> message, String alias) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().post(message, id, alias);
		else {
			if (alias!=null) {
				List<UUID> destinations = system.getActorsFromAlias(alias);

				UUID dest = null;
				if (!destinations.isEmpty()) {
					if (destinations.size()==1)
						dest = destinations.get(0);
					else
						dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
				}
				dest = (dest!=null) ? dest : ActorMessageDispatcher.ALIAS_ID();
				system.getBufferQueue().offer(message.copy(dest));
			}
		}
	}
	
	@Override
	public void unsafe_send(ActorMessage<?> message) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().unsafe_post(message, id);
		else
			system.getBufferQueue().offer(message.copy());
	}
	
	@Override
	public void unsafe_send(ActorMessage<?> message, String alias) {
		if (system.getMessagingEnabled().get())
			system.getMessageDispatcher().unsafe_post(message, id, alias);
		else {
			if (alias!=null) {
				List<UUID> destinations = system.getActorsFromAlias(alias);

				UUID dest = null;
				if (!destinations.isEmpty()) {
					if (destinations.size()==1)
						dest = destinations.get(0);
					else
						dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
				}
				dest = (dest!=null) ? dest : ActorMessageDispatcher.ALIAS_ID();
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
			Actor sourceActor = system.getCells().get(message.source()).getActor();
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
	public UUID internal_addChild(InternalActorCell cell) {
		cell.setParent(id);
		children.add(cell.getId());
		system.internal_addCell(cell);
		
		return cell.getId();
	}
	
	@Override
	public UUID addChild(ActorFactory factory) {
		InternalActorCell cell = system.generateCell(factory.create());
		((InternalActorRuntimeSystem)system).getContainer().register(cell.getId(), factory);
		
		return internal_addChild(cell);
	}
	
	@Override
	public List<UUID> addChild(ActorFactory factory, int instances) {
		List<UUID> result = new ArrayList<>(instances);
		
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
		recoverProtocol.apply();
		actor.preStart();
	}
	
	@Override
	public void preRestart(Exception reason) {
		actor.preRestart(reason);
	}
	
	@Override
	public void postRestart(Exception reason) {
		actor.postRestart(reason);
	}
	
	@Override
	public void postStop() {
		actor.postStop();
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
	public void internal_stop() {
		if (parent!=null)
			system.getCells().get(parent).getChildren().remove(id);
		/*if (!(actor instanceof ResourceActor) && !(actor instanceof PseudoActor)) @See: ActorMessageDispatcher */
			system.getMessageDispatcher().unregisterCell(this);
		system.removeActor(id);
		
		Iterator<UUID> iterator = deathWatcher.iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			system.sendAsDirective(ActorMessage.create(null, INTERNAL_STOP_SUCCESS, id, dest));
		}
	}
	
	@Override
	public void watch(UUID dest) {
		InternalActorCell cell = system.getCells().get(dest);
		if (cell!=null)
			cell.getDeathWatcher().add(id);
	}
	
	@Override
	public void unwatch(UUID dest) {
		InternalActorCell cell = system.getCells().get(dest);
		if (cell!=null)
			cell.getDeathWatcher().remove(id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, E... events) {	
		if (system.getConfig().persistenceMode() && events!=null) {
			List<ActorPersistenceDTO<Object>> list = new ArrayList<>(events.length);
			for (int i=0; i<events.length; i++)
				list.add(new ActorPersistenceDTO<>(events[i], persistenceId(), System.currentTimeMillis(), 0));
			PersistenceTuple tuple = new PersistenceTuple((Consumer<Object>)onSuccess, onFailure, Arrays.asList(events));
			system.getMessageDispatcher().postPersistence(ActorMessage.create(new ImmutableList<>(list), PersistenceServiceActor.PERSIST_EVENTS, id, null));
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
			system.getMessageDispatcher().postPersistence(ActorMessage.create(list.get(0), PersistenceServiceActor.PERSIST_STATE, id, null));
			persistenceTuples.offer(tuple);
		}
	}
	
	public void recover(ActorMessage<?> message) {
		if (system.getConfig().persistenceMode() && actor instanceof PersistentActor) {
			((PersistentActor<?, ?>)actor).recover(message.valueAsString());
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
	public Queue<Long> getProcessingTimeStatistics() {
		return processingTimeStatistics;
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
