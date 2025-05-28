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
package io.actor4j.core.runtime;

import static io.actor4j.core.runtime.protocols.ActorProtocolTag.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import io.actor4j.core.ActorCell;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.config.ActorServiceConfig;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.runtime.pods.DefaultPodReplicationController;
import io.actor4j.core.runtime.pods.PodReplicationController;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.core.utils.ActorTimer;
import io.actor4j.core.utils.PodActorFactory;

public abstract class ActorSystemImpl implements InternalActorRuntimeSystem {
	protected /*Changeable only before starting*/ ActorSystemConfig config;
	
//	protected /*quasi final*/ DIContainer<ActorId> container; // Currently not used! @See ActorCell
	protected /*quasi final*/ PodReplicationController podReplicationController;
	protected /*quasi final*/ PodReplicationControllerRunnableFactory podReplicationControllerRunnableFactory;
	protected /*quasi final*/ WatchdogRunnableFactory watchdogRunnableFactory;
	
	protected final PseudoActorCellFactory pseudoActorCellFactory;

	protected final Map<UUID, ActorId> exposedCells; // globalId -> ActorId
	protected final Map<String, Queue<ActorId>> aliases;  // ActorCellAlias -> ActorCellId
	protected final Map<ActorId, String> hasAliases;
	protected final Map<ActorId, Boolean> resourceCells;
	protected final Map<ActorId, Boolean> podCells;
	protected final Map<String, Queue<ActorId>> podDomains; // PodActorCellDomain -> ActorCellId
	protected final Map<ActorId, InternalActorCell> pseudoCells;
	protected /*quasi final*/ ActorMessageDispatcher messageDispatcher;
	
	protected final AtomicBoolean messagingEnabled;
	protected final AtomicBoolean shutdownHookTriggered;
	
	protected final Queue<ActorMessage<?>> bufferQueue;
	protected final ActorExecutorService executorService;
	
	protected final ActorStrategyOnFailure strategyOnFailure;
	
	protected final AtomicReference<CountDownLatch> countDownLatch;
	protected final AtomicInteger countDownLatchPark;

	protected /*quasi final*/ ActorId ZERO_ID;
	protected final ActorId ALIAS_ID = ZERO_ID;
	
	protected /*quasi final*/ ActorId USER_ID;
	protected /*quasi final*/ ActorId SYSTEM_ID;
	protected /*quasi final*/ ActorId UNKNOWN_ID;
	protected /*quasi final*/ ActorId PSEUDO_ID;
	
	public ActorSystemImpl(ActorSystemConfig config) {
		super();

		if (config!=null)
			this.config = config;
		else
			this.config = ActorSystemConfig.create();
		
//		container = DefaultDIContainer.create();
		podReplicationController = new DefaultPodReplicationController(this);
		podReplicationControllerRunnableFactory = (system) -> new DefaultPodReplicationControllerRunnable(system);
		watchdogRunnableFactory = (system, actors) -> new DefaultWatchdogRunnable(system, actors);
		
		pseudoActorCellFactory = (system, actor, blocking) -> new PseudoActorCell(system, actor, blocking);

		exposedCells   = new ConcurrentHashMap<>();
		aliases        = new ConcurrentHashMap<>();
		hasAliases     = new ConcurrentHashMap<>();
		resourceCells  = new ConcurrentHashMap<>();
		podCells       = new ConcurrentHashMap<>();
		podDomains     = new ConcurrentHashMap<>();
		pseudoCells    = new ConcurrentHashMap<>();
		
		messagingEnabled = new AtomicBoolean();
		shutdownHookTriggered = new AtomicBoolean();
		
		bufferQueue = createLockFreeLinkedQueue();
		executorService = createActorExecutorService();
		
		strategyOnFailure = new DefaultActorStrategyOnFailure(this);
		
		countDownLatch = new AtomicReference<>();
		countDownLatchPark = new AtomicInteger();
		
		resetCells();
	}
	
	@Override
	public <T> Queue<T> createLockFreeLinkedQueue() {
		return new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public ActorId ZERO_ID() {
		return ZERO_ID;
	}
	
	@Override
	public ActorId ALIAS_ID() {
		return ALIAS_ID;
	}
	
	@Override
	public ActorId USER_ID() {
		return USER_ID;
	}

	@Override
	public ActorId SYSTEM_ID() {
		return SYSTEM_ID;
	}
	
	@Override
	public ActorId UNKNOWN_ID() {
		return UNKNOWN_ID;
	}
	
	@Override
	public ActorId PSEUDO_ID() {
		return PSEUDO_ID;
	}
	
	protected void reset() {
		messagingEnabled.set(false);

		aliases.clear();
		hasAliases.clear();
		resourceCells.clear();
		pseudoCells.clear();
		
		bufferQueue.clear();
	
		resetCells();
	}
	
	@Override
	public boolean isEmpty() {
		return ((ActorCell)USER_ID).getChildren().size()==0
			&& ((ActorCell)SYSTEM_ID).getChildren().size()==0;
	}
	
	protected void resetCells() {
		resetCountdownLatch();
		
		ZERO_ID = internal_addCell(createActorCell(new Actor("zero") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		}));
		
		USER_ID = internal_addCell(createActorCell(new Actor("user") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
			
			@Override
			public void postStop() {
				if (config.threadMode()==ActorThreadMode.PARK)
					countDownLatchPark.decrementAndGet();
				else
					countDownLatch.get().countDown();
			}
		}));
		
		SYSTEM_ID = internal_addCell(createActorCell(new Actor("system") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
			
			@Override
			public void postStop() {
				if (config.threadMode()==ActorThreadMode.PARK)
					countDownLatchPark.decrementAndGet();
				else
					countDownLatch.get().countDown();
			}
		}));
		
		UNKNOWN_ID = internal_addCell(createActorCell(new Actor("unknown") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
			
			@Override
			public void postStop() {
				if (config.threadMode()==ActorThreadMode.PARK)
					countDownLatchPark.decrementAndGet();
				else
					countDownLatch.get().countDown();
			}
		}));
		
		PSEUDO_ID = internal_addCell(createActorCell(new Actor("pseudo") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		}));
	}
	
	protected void resetCountdownLatch() {
		if (config.threadMode()==ActorThreadMode.PARK)
			countDownLatchPark.set(3);
		else
			countDownLatch.set(new CountDownLatch(3));
	}
	
	public InternalActorCell generateCell(Actor actor) {
		if (actor instanceof ResourceActor)
			return createResourceActorCell(actor);
		else if (actor instanceof PodActor)
			return createPodActorCell(actor);
		else
			return createActorCell(actor);
	}
	
	public InternalActorCell generateCell(Class<? extends Actor> clazz) {
		if (clazz==ResourceActor.class)
			return createResourceActorCell(null);
		else if (clazz==PodActor.class)
			return createPodActorCell(null);
		else
			return createActorCell(null);
	}
	
	protected abstract InternalActorCell createResourceActorCell(Actor actor);
	protected abstract InternalActorCell createActorCell(Actor actor);
	protected abstract InternalActorCell createPodActorCell(Actor actor);

	@Override
	public ActorSystemConfig getConfig() {
		return config;
	}
	
	@Override
	public boolean setConfig(ActorSystemConfig config) {
		boolean result = false;
		
		if (!executorService.isStarted() && config!=null) {
			this.config = config;
			resetCountdownLatch();
			result = true;
		}
		
		return result;
	}
	
	@Override
	public boolean setConfig(ActorServiceConfig config) {
		boolean result = false;
		
		if (!executorService.isStarted() && config!=null) {
			this.config = config;
			resetCountdownLatch();
			result = true;
		}
		
		return result;
	}
	
	protected abstract ActorExecutorService createActorExecutorService();
	
	@Override
	public PodReplicationController getPodReplicationController() {
		return podReplicationController;
	}
	
	@Override
	public PodReplicationControllerRunnableFactory getPodReplicationControllerRunnableFactory() {
		return podReplicationControllerRunnableFactory;
	}

	@Override
	public WatchdogRunnableFactory getWatchdogRunnableFactory() {
		return watchdogRunnableFactory;
	}
	
	@Override
	public PseudoActorCellFactory getPseudoActorCellFactory() {
		return pseudoActorCellFactory;
	}
	
	@Override
	public Map<UUID, ActorId> getExposedCells() {
		return exposedCells;
	}
	
	@Override
	public Map<ActorId, InternalActorCell> getPseudoCells() {
		return pseudoCells;
	}
	
	@Override
	public Map<ActorId, Boolean> getResourceCells() {
		return resourceCells;
	}
	
	@Override
	public Map<ActorId, Boolean> getPodCells() {
		return podCells;
	}

	@Override
	public Map<String, Queue<ActorId>> getPodDomains() {
		return podDomains;
	}

	@Override
	public Map<String, Queue<ActorId>> getAliases() {
		return aliases;
	}

	@Override
	public ActorMessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	@Override
	public ActorStrategyOnFailure getStrategyOnFailure() {
		return strategyOnFailure;
	}
	
	@Override
	public AtomicBoolean getMessagingEnabled() {
		return messagingEnabled;
	}

	@Override
	public Queue<ActorMessage<?>> getBufferQueue() {
		return bufferQueue;
	}
	
	@Override
	public ActorExecutorService getExecutorService() {
		return executorService;
	}
	
	@Override
	public ActorId internal_addCell(InternalActorCell cell) {
		Actor actor = cell.getActor();
		switch (cell.getType()) {
			case ActorCell.RESOURCE_ACTOR_CELL:
				resourceCells.put(cell.getId(), false);
			case ActorCell.DEFAULT_ACTOR_CELL:
				actor.setCell(cell);
				if (cell.isPod())
					podCells.put(cell.getId(), false);
				if (executorService.isStarted()) {
					/* if (!(actor instanceof ResourceActor)) @See: ActorMessageDispatcher */
						messageDispatcher.registerCell(cell);
					/* preStart */
					internal_preStart(cell);
				};
				break;
			case ActorCell.PSEUDO_ACTOR_CELL:
				pseudoCells.put(cell.getId(), cell);
				break;
			default:
				break;
		}

		return cell.getId();
	}
	
	public void internal_preStart(InternalActorCell cell) {
		try {
			cell.preStart();
		}
		catch(Exception e) {
			getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.ACTOR, cell.getId());
			getStrategyOnFailure().handle(cell, e);
		}
	}

	protected ActorId user_addCell(InternalActorCell cell) {
		cell.setParent(USER_ID);
		((ActorCell)USER_ID).getChildren().add(cell.getId());
		return internal_addCell(cell);
	}
	
	protected ActorId system_addCell(InternalActorCell cell) {
		cell.setParent(SYSTEM_ID);
		((ActorCell)SYSTEM_ID).getChildren().add(cell.getId());
		return internal_addCell(cell);
	}
	
	@Override
	public ActorId pseudo_addCell(InternalActorCell cell) {
		cell.setParent(PSEUDO_ID);
		((ActorCell)PSEUDO_ID).getChildren().add(cell.getId());
		return internal_addCell(cell);
	}
	
	@Override
	public ActorId addActor(ActorFactory factory) {
		InternalActorCell cell = generateCell(factory.create());
		cell.setFactory(factory);
		
		return user_addCell(cell);
	}
	
	@Override
	public List<ActorId> addActor(ActorFactory factory, int instances) {
		List<ActorId> result = new ArrayList<>(instances);
		
		for (int i=0; i<instances; i++)
			result.add(addActor(factory));
			
		return result;
	}
	
	@Override
	public ActorId addSystemActor(ActorFactory factory) {
		InternalActorCell cell = generateCell(factory.create());
		cell.setFactory(factory);
		
		return system_addCell(cell);
	}
	
	@Override
	public List<ActorId> addSystemActor(ActorFactory factory, int instances) {
		List<ActorId> result = new ArrayList<>(instances);
		
		for (int i=0; i<instances; i++)
			result.add(addSystemActor(factory));
			
		return result;
	}
	
	@Override
	public ActorId deployActor(ActorFactory factory) {
		return addActor(factory);
	}
	
	@Override
	public void undeployActor(ActorId id) {
		sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, SYSTEM_ID, id));
	}
	
	@Override
	public void undeployActors(String alias) {
		List<ActorId> ids = getActorsFromAlias(alias);
		for (ActorId id : ids)
			undeployActor(id);
	}
	
	public void setPodDomain(ActorId id, String domain) {
		if (id!=null && domain!=null && !domain.isEmpty()) {
			Queue<ActorId> queue = null;
			if ((queue=podDomains.get(domain))==null) {
				queue = new ConcurrentLinkedQueue<>();
				queue.add(id);
				podDomains.put(domain, queue);
			}	
			else {
				queue.add(id);
			}
		}
	}
	
	@Override
	public ActorId addPodActor(PodActorFactory factory, PodContext context) {
		InternalPodActorCell cell = (InternalPodActorCell)generateCell(factory.create());
		cell.setContext(context);
		setPodDomain(cell.getId(), context.domain());
		cell.setFactory(factory);
		
		return user_addCell(cell);
	}
	
	@Override
	public void deployPods(File jarFile, PodConfiguration podConfiguration) {
		podReplicationController.deployPods(jarFile, podConfiguration);
	}
	
	@Override
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration) {
		podReplicationController.deployPods(factory, podConfiguration);
	}
	
	@Override
	public void undeployPods(String domain) {
		podReplicationController.undeployPods(domain);
	}
	
	@Override
	public boolean primaryPodDeployed(String domain) {
		Queue<ActorId> queue = podDomains.get(domain);
		
		return queue.stream().filter(id -> {
				PodActorCell cell = (PodActorCell)id;
				return cell.getContext().primaryReplica();
			}).findFirst().isPresent();
	}
	
	public boolean updateActors(String alias, ActorFactory factory) {
		return updateActors(alias, factory, 1);
	}
	
	public boolean updateActors(String alias, ActorFactory factory, int instances) {
		boolean result = false;
		if (instances>0) {
			List<ActorId> oldActors = getActorsFromAlias(alias);
			/*
			if (oldActors.size()>0 && getCells().get(oldActors.get(0)).actor instanceof ActorVersionNumber) {	
				Actor newActor = factory.create();
				if (newActor instanceof ActorVersionNumber) {
					VersionNumber oldActorsVersionNumber = VersionNumber.parse((ActorVersionNumber)getCells().get(oldActors.get(0)).actor).versionNumber());
					VersionNumber newActorsVersionNumber = VersionNumber.parse((ActorVersionNumber)newActor).versionNumber());
						
					if (newActorsVersionNumber.compareTo(oldActorsVersionNumber)<=0)
						return false;
				}
				else
					return false;
			}
			*/
			
			List<ActorId> newActors = addActor(factory, instances);
			setAlias(newActors, alias);
			if (oldActors.size()>0)
				broadcast(ActorMessage.create(null, Actor.STOP, SYSTEM_ID, null), new ActorGroupSet(oldActors));
			result = true;
		}
		
		return result;
	}
	
	@Override
	public void removeActor(ActorId id) {
		InternalActorCell cell = (InternalActorCell)id;
		if (cell.getType()==ActorCell.DEFAULT_ACTOR_CELL) {
			if (cell.isPod())
				podCells.remove(id);
		}
		else if (cell.getType()==ActorCell.RESOURCE_ACTOR_CELL)
			resourceCells.remove(id);
		else if (cell.getType()==ActorCell.PSEUDO_ACTOR_CELL)
			pseudoCells.remove(id);
		
		String alias = null;
		if ((alias=hasAliases.get(id))!=null) {
			hasAliases.remove(id);
			Queue<ActorId> queue = aliases.get(alias);
			queue.remove(id);
			if (queue.isEmpty())
				aliases.remove(alias);
		}
	}

	@Override
	public boolean hasActor(String globalId) {
		boolean result = false;
		try {
			UUID uuid = UUID.fromString(globalId);
			result = exposedCells.get(uuid)!=null;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
		
		return result;
	}		
	
	@Override
	public ActorId getActor(UUID globalId) {
		return exposedCells.get(globalId);
	}
	
	@Override
	public ActorSystemImpl setAlias(ActorId id, String alias) {
		if (id!=null && alias!=null && !alias.isEmpty()) {
			Queue<ActorId> queue = null;
			if ((queue=aliases.get(alias))==null) {
				queue = new ConcurrentLinkedQueue<>();
				queue.add(id);
				aliases.put(alias, queue);
				hasAliases.put(id, alias);
			}	
			else {
				queue.add(id);
				hasAliases.put(id, alias);
			}
		}
		
		return this;
	}
	
	@Override
	public ActorSystemImpl setAlias(List<ActorId> ids, String alias) {
		for (ActorId id : ids)
			setAlias(id, alias);
		
		return this;
	}
	
	@Override
	public ActorId getActorFromAlias(String alias) {
		List<ActorId> result = getActorsFromAlias(alias);
		
		return !result.isEmpty() ? result.get(0) : null;
	}
	
	@Override
	public List<ActorId> getActorsFromAlias(String alias) {
		List<ActorId> result = new LinkedList<>();
		
		Queue<ActorId> queue = aliases.get(alias);
		if (queue!=null)
			queue.forEach((id) -> result.add(id));
		
		return result;
	}
	
	@Override
	public String getAliasFromActor(ActorId id) {
		String result = null;
		
		Iterator<Entry<String, Queue<ActorId>>> iteratorAliases = aliases.entrySet().iterator();
		outer: while (iteratorAliases.hasNext()) {
			Entry<String, Queue<ActorId>> entry = iteratorAliases.next();
			Iterator<ActorId> iteratorQueue = entry.getValue().iterator();
			while (iteratorQueue.hasNext()) {
				if (id==iteratorQueue.next()) {
					result = entry.getKey();
					break outer;
				}
			}
		}
			
		return result;
	}
	
	@Override
	public String getActorPath(ActorId id) {
		String result = null;
		
		if (id!=null) {
			if (id==USER_ID)
				result = "/";
			else {
				StringBuffer buffer = new StringBuffer();
				InternalActorCell cell = (InternalActorCell)id;
				if (cell.getActor()!=null)
					buffer.append("/" + (cell.getActor().getName()!=null ? cell.getActor().getName():cell.getActor().getId().toString()));
				ActorId parent = null;
				while ((parent=cell.getParent())!=null && parent!=USER_ID) {
					cell = (InternalActorCell)parent;
					buffer.insert(0, "/" + (cell.getActor().getName()!=null ? cell.getActor().getName():cell.getActor().getId().toString()));
				}
				
				result = buffer.toString();
			}
		}
		
		return result;
	}
	
	@Override
	public ActorId getActorFromPath(String path) {
		InternalActorCell result = null;
		
		if (path!=null) {
			InternalActorCell parent = (InternalActorCell)USER_ID;
			
			if (path.isEmpty() || path.equals("/"))
				result = parent;
			else {
				StringTokenizer tokenizer = new StringTokenizer(path, "/");
				String token = null;
				while (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					
					Iterator<ActorId> iterator = parent.getChildren().iterator();
					result  = null;
					while (iterator.hasNext()) {
						InternalActorCell child = (InternalActorCell)iterator.next();
						if (child!=null  && (token.equals(child.getActor().getName()) || token.equals(child.getId().toString()))) {
							result = child;
							break;
						}
					}
					if (result==null)
						break;
					parent = result;
				}
			}
		}
		
		return (result!=null) ? result.getId() : null;
	}
	
	@Override
	public ActorSystemImpl send(ActorMessage<?> message) {
		if (!messagingEnabled.get()) 
			bufferQueue.offer(message.copy());
		else
			messageDispatcher.postOuter(message);
		
		return this;
	}
	
	@Override
	public ActorSystemImpl sendViaPath(ActorMessage<?> message, String path) {
		ActorId dest = getActorFromPath(path);
		if (dest!=null)
			send(message.shallowCopy(dest));
		
		return this;
	}
	
	@Override
	public ActorSystemImpl sendViaAlias(ActorMessage<?> message, String alias) {
		List<ActorId> destinations = getActorsFromAlias(alias);
		
		if (!destinations.isEmpty()) {
			ActorId dest = null;
			
			if (destinations.size()==1)
				dest = destinations.get(0);
			else 
				dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			if (dest!=null)
				send(message.shallowCopy(dest));
		}
		
		return this;
	}
	
	@Override
	public boolean sendViaAliasAsServer(ActorMessage<?> message, String alias) {
		boolean result = false;
		
		List<ActorId> destinations = getActorsFromAlias(alias);
		if (!destinations.isEmpty()) {
			ActorId dest = null;
			
			if (destinations.size()==1)
				dest = destinations.get(0);
			else 
				dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			if (dest!=null) {
				sendAsServer(message.shallowCopy(dest));
				result = true;
			}
		}
		
		return result;
	}
	
	@Override
	public ActorSystemImpl sendWhenActive(ActorMessage<?> message) {
		if (executorService.isStarted() && messagingEnabled.get() && message!=null && message.dest()!=null)  {
			InternalActorCell cell = (InternalActorCell)message.dest();
			if (cell.isActive())
				messageDispatcher.postOuter(message);
			else
				((ActorTimerExecutorService)executorService.globalTimer()).schedule(new Runnable() {
					@Override
					public void run() {
						if (cell.isActive()) {
							messageDispatcher.postOuter(message);
							throw new RuntimeException("Task canceled"); // cancel
						}
					}
				}, 25, 25, TimeUnit.MILLISECONDS);
		}
		
		return this;
	}
	
	@Override
	public void sendAsServer(ActorMessage<?> message) {
		if (message.dest().localId()==null && message.dest().globalId()!=null) {
			ActorId dest = exposedCells.get(message.dest().globalId());
			if (dest!=null) {
				if (!messagingEnabled.get()) 
					bufferQueue.offer(message.copy(dest));
				else
					messageDispatcher.postServer(message.shallowCopy(dest));
			}
		}
		else {
			if (!messagingEnabled.get()) 
				bufferQueue.offer(message.copy());
			else
				messageDispatcher.postServer(message);
		}
	}
	
	@Override
	public void sendAsDirective(ActorMessage<?> message) {
		if (messagingEnabled.get()) 
			messageDispatcher.postDirective(message);
	}
	
	@Override
	public ActorSystemImpl broadcast(ActorMessage<?> message, ActorGroup group) {
		if (!messagingEnabled.get())
			for (ActorId id : group)
				bufferQueue.offer(message.copy(id));
		else
			for (ActorId id : group)
				messageDispatcher.postOuter(message.shallowCopy(id));
		
		return this;
	}
	
	@Override
	public ActorId getRedirectionDestination(ActorId source) {
		return ((InternalActorCell)source).getRedirect();
	}
	
	@Override
	public ActorSystemImpl addRedirection(ActorId source, ActorId dest) {
		((InternalActorCell)source).setRedirect(dest);
		
		return this;
	}
	
	@Override
	public ActorSystemImpl removeRedirection(ActorId source) {
		((InternalActorCell)source).setRedirect(null);
		
		return this;
	}
	
	
	@Override
	public ActorTimer timer() {
		return executorService.timer();
	}
	
	@Override
	public ActorTimer globalTimer() {
		return executorService.globalTimer();
	}
	
	@Override
	public boolean start() {
		return start(null, null);
	}
	
	@Override
	public boolean internal_iterateCell(InternalActorCell root, Function<InternalActorCell, Boolean> action) {
		boolean result = false;
		
		Stack<InternalActorCell> stack = new Stack<>();
		stack.push(root);
		
		while (!stack.isEmpty()) {
			InternalActorCell cell = stack.pop();
			if (action.apply(cell)) {
				result = true;
				break;
			}

			Iterator<ActorId> iterator = cell.getChildren().iterator();
			while(iterator.hasNext()) {
				stack.push((InternalActorCell)iterator.next());
			}
		}
		
		return result;
	}
	
	@Override
	public boolean start(Runnable onStartup, Runnable onTermination) {
		boolean result = false;
		
		if (!executorService.isStarted()) {
			executorService.start(new Runnable() {
				@Override
				public void run() {
					messageDispatcher.registerCell((InternalActorCell)PSEUDO_ID);
					/* preStart */
					Function<InternalActorCell, Boolean> preStart = cell -> {
						if (cell.isRootInUser() || cell.isRootInSystem() )
							internal_preStart(cell);
						
						return false;
					};
					internal_iterateCell((InternalActorCell)USER_ID, preStart);
					internal_iterateCell((InternalActorCell)SYSTEM_ID, preStart);
					
					messagingEnabled.set(true);
					
					ActorMessage<?> message = null;
					while ((message=bufferQueue.poll())!=null)
						messageDispatcher.postOuter(message);
					
					if (onStartup!=null)
						onStartup.run();
				}
			}, onTermination);
			
			result = true;
		}
		
		return result;
	}
	
	@Override
	public boolean isShutdownHookTriggered() {
		return shutdownHookTriggered.get();
	}
	
	@Override
	public void shutdownHookTriggered() {
		shutdownHookTriggered.set(true);
	}
	
	@Override
	public void shutdownWithActors() {
		shutdownWithActors(false);
	}
	
	@Override
	public void shutdownWithActors(final boolean await) {
		if (executorService.isStarted()) {
			Thread waitOnTermination = new Thread(new Runnable() {
				@Override
				public void run() {
					sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, SYSTEM_ID, USER_ID));
					sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, SYSTEM_ID, SYSTEM_ID));
					sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, SYSTEM_ID, UNKNOWN_ID));
					if (config.threadMode()==ActorThreadMode.PARK) {
						while (countDownLatchPark.get()>0) {
							try {
								Thread.sleep(config.sleepTime());
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					}
					else {
						try {
							countDownLatch.get().await();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					messageDispatcher.unregisterCell((InternalActorCell)PSEUDO_ID);
					removeActor(PSEUDO_ID);
					
					executorService.shutdown(await);
				}
			});
			
			waitOnTermination.start();
			if (await)
				try {
					waitOnTermination.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			
			reset();
		}
	}
	
	@Override
	public void shutdown() {
		shutdown(false);
	}
	
	@Override
	public void shutdown(boolean await) {
		if (executorService.isStarted())
			executorService.shutdown(await);
	}
}
