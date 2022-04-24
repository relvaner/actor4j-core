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

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.PseudoActor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.config.ActorServiceConfig;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.runtime.di.DIContainer;
import io.actor4j.core.runtime.di.DefaultDIContainer;
import io.actor4j.core.runtime.pods.DefaultPodReplicationController;
import io.actor4j.core.runtime.pods.PodReplicationController;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.core.utils.ActorTimer;
import io.actor4j.core.utils.PodActorFactory;

public abstract class ActorSystemImpl implements InternalActorRuntimeSystem {
	protected /*Changeable only before starting*/ ActorSystemConfig config;
	
	protected /*quasi final*/ DIContainer<UUID> container;
	protected /*quasi final*/ PodReplicationController podReplicationController;
	protected /*quasi final*/ PodReplicationControllerRunnableFactory podReplicationControllerRunnableFactory;
	protected /*quasi final*/ WatchdogRunnableFactory watchdogRunnableFactory;
	
	protected final PseudoActorCellFactory pseudoActorCellFactory;
	
	protected final Map<UUID, InternalActorCell> cells; // ActorCellID    -> ActorCell
	protected final Map<String, Queue<UUID>> aliases;  // ActorCellAlias -> ActorCellID
	protected final Map<UUID, String> hasAliases;
	protected final Map<UUID, Boolean> resourceCells;
	protected final Map<UUID, Boolean> podCells;
	protected final Map<String, Queue<UUID>> podDomains; // PodActorCellDomain -> ActorCellID
	protected final Map<UUID, InternalActorCell> pseudoCells;
	protected final Map<UUID, UUID> redirector;
	protected /*quasi final*/ ActorMessageDispatcher messageDispatcher;
	protected /*quasi final*/ ActorThreadFactory actorThreadFactory;
	
	protected final AtomicBoolean messagingEnabled;
	
	protected final Queue<ActorMessage<?>> bufferQueue;
	protected final ActorExecuterService executerService;
	
	protected final ActorStrategyOnFailure actorStrategyOnFailure;
	
	protected final AtomicReference<CountDownLatch> countDownLatch;
	protected final AtomicInteger countDownLatchPark;
	
	protected final UUID USER_ID;
	protected final UUID SYSTEM_ID;
	protected final UUID UNKNOWN_ID;
	protected final UUID PSEUDO_ID;
	
	public ActorSystemImpl(ActorSystemConfig config) {
		super();

		if (config!=null)
			this.config = config;
		else
			this.config = ActorSystemConfig.create();
		
		container = DefaultDIContainer.create();
		podReplicationController = new DefaultPodReplicationController(this);
		podReplicationControllerRunnableFactory = (system) -> new DefaultPodReplicationControllerRunnable(system);
		watchdogRunnableFactory = (system, actors) -> new DefaultWatchdogRunnable(system, actors);
		
		pseudoActorCellFactory = (system, actor, blocking) -> new PseudoActorCell(system, actor, blocking);
		
		cells          = new ConcurrentHashMap<>();
		aliases        = new ConcurrentHashMap<>();
		hasAliases     = new ConcurrentHashMap<>();
		resourceCells  = new ConcurrentHashMap<>();
		podCells       = new ConcurrentHashMap<>();
		podDomains     = new ConcurrentHashMap<>();
		pseudoCells    = new ConcurrentHashMap<>();
		redirector     = new ConcurrentHashMap<>();
		
		messagingEnabled = new AtomicBoolean();
		
		bufferQueue = new ConcurrentLinkedQueue<>();
		executerService = createActorExecuterService();
		
		actorStrategyOnFailure = new DefaultActorStrategyOnFailure(this);
		
		countDownLatch = new AtomicReference<>();
		countDownLatchPark = new AtomicInteger();
				
		USER_ID    = UUID.randomUUID();
		SYSTEM_ID  = UUID.randomUUID();
		UNKNOWN_ID = UUID.randomUUID();
		PSEUDO_ID  = UUID.randomUUID();
		
		resetCells();
	}
	
	@Override
	public UUID USER_ID() {
		return USER_ID;
	}

	@Override
	public UUID SYSTEM_ID() {
		return SYSTEM_ID;
	}
	
	@Override
	public UUID UNKNOWN_ID() {
		return UNKNOWN_ID;
	}
	
	@Override
	public UUID PSEUDO_ID() {
		return PSEUDO_ID;
	}
	
	protected void reset() {
		messagingEnabled.set(false);
		
		cells.clear();
		aliases.clear();
		hasAliases.clear();
		resourceCells.clear();
		pseudoCells.clear();
		redirector.clear();
		
		bufferQueue.clear();
	
		resetCells();
	}
	
	protected void resetCells() {
		resetCountdownLatch();
		
		internal_addCell(createActorCell(new Actor("user") {
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
		}, USER_ID));
		
		internal_addCell(createActorCell(new Actor("system") {
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
		}, SYSTEM_ID));
		
		internal_addCell(createActorCell(new Actor("unknown") {
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
		}, UNKNOWN_ID));
		
		internal_addCell(createActorCell(new Actor("pseudo") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		}, PSEUDO_ID));
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
	protected abstract InternalActorCell createActorCell(Actor actor, UUID id);
	protected abstract InternalActorCell createPodActorCell(Actor actor);

	@Override
	public ActorSystemConfig getConfig() {
		return config;
	}
	
	@Override
	public boolean setConfig(ActorSystemConfig config) {
		boolean result = false;
		
		if (!executerService.isStarted() && config!=null) {
			this.config = config;
			resetCountdownLatch();
			result = true;
		}
		
		return result;
	}
	
	@Override
	public boolean setConfig(ActorServiceConfig config) {
		boolean result = false;
		
		if (!executerService.isStarted() && config!=null) {
			this.config = config;
			resetCountdownLatch();
			result = true;
		}
		
		return result;
	}
	
	protected abstract ActorExecuterService createActorExecuterService();

	@Override
	public DIContainer<UUID> getContainer() {
		return container;
	}
	
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
	public Map<UUID, InternalActorCell> getCells() {
		return cells;
	}
	
	@Override
	public Map<UUID, InternalActorCell> getPseudoCells() {
		return pseudoCells;
	}
	
	@Override
	public Map<UUID, Boolean> getResourceCells() {
		return resourceCells;
	}
	
	@Override
	public Map<UUID, Boolean> getPodCells() {
		return podCells;
	}

	@Override
	public Map<String, Queue<UUID>> getPodDomains() {
		return podDomains;
	}

	@Override
	public Map<String, Queue<UUID>> getAliases() {
		return aliases;
	}
	
	@Override
	public Map<UUID, UUID> getRedirector() {
		return redirector;
	}

	@Override
	public ActorMessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	public ActorThreadFactory getActorThreadFactory() {
		return actorThreadFactory;
	}

	public void setActorThreadFactory(ActorThreadFactory actorThreadFactory) {
		this.actorThreadFactory = actorThreadFactory;
	}

	@Override
	public ActorStrategyOnFailure getActorStrategyOnFailure() {
		return actorStrategyOnFailure;
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
	public ActorExecuterService getExecuterService() {
		return executerService;
	}
	
	@Override
	public UUID internal_addCell(InternalActorCell cell) {
		Actor actor = cell.getActor();
		if (actor instanceof PseudoActor)
			pseudoCells.put(cell.getId(), cell);
		else {
			actor.setCell(cell);
			cells.put(cell.getId(), cell);
			if (actor instanceof ResourceActor)
				resourceCells.put(cell.getId(), false);
			else if (actor instanceof PodActor)
				podCells.put(cell.getId(), false);
			if (executerService.isStarted()) {
				if (!(actor instanceof ResourceActor))
					messageDispatcher.registerCell(cell);
				/* preStart */
				cell.preStart();
			}
		}
		return cell.getId();
	}

	protected UUID user_addCell(InternalActorCell cell) {
		cell.setParent(USER_ID);
		cells.get(USER_ID).getChildren().add(cell.getId());
		return internal_addCell(cell);
	}
	
	protected UUID system_addCell(InternalActorCell cell) {
		cell.setParent(SYSTEM_ID);
		cells.get(SYSTEM_ID).getChildren().add(cell.getId());
		return internal_addCell(cell);
	}
	
	@Override
	public UUID pseudo_addCell(InternalActorCell cell) {
		cell.setParent(PSEUDO_ID);
		cells.get(PSEUDO_ID).getChildren().add(cell.getId());
		return internal_addCell(cell);
	}
	
	@Override
	public UUID addActor(ActorFactory factory) {
		InternalActorCell cell = generateCell(factory.create());
		container.register(cell.getId(), factory);
		
		return user_addCell(cell);
	}
	
	@Override
	public List<UUID> addActor(ActorFactory factory, int instances) {
		List<UUID> result = new ArrayList<>(instances);
		
		for (int i=0; i<instances; i++)
			result.add(addActor(factory));
			
		return result;
	}
	
	@Override
	public UUID addSystemActor(ActorFactory factory) {
		InternalActorCell cell = generateCell(factory.create());
		container.register(cell.getId(), factory);
		
		return system_addCell(cell);
	}
	
	@Override
	public List<UUID> addSystemActor(ActorFactory factory, int instances) {
		List<UUID> result = new ArrayList<>(instances);
		
		for (int i=0; i<instances; i++)
			result.add(addSystemActor(factory));
			
		return result;
	}
	
	public void setPodDomain(UUID id, String domain) {
		if (id!=null && domain!=null && !domain.isEmpty()) {
			Queue<UUID> queue = null;
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
	public UUID addPodActor(PodActorFactory factory, PodContext context) {
		InternalPodActorCell cell = (InternalPodActorCell)generateCell(factory.create());
		cell.setContext(context);
		setPodDomain(cell.getId(), context.domain());
		container.register(cell.getId(), factory);
		
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
		Queue<UUID> queue = podDomains.get(domain);
		
		return queue.stream().filter(id -> {
				PodActorCell cell = ((PodActorCell)cells.get(id));
				return cell.getContext().primaryReplica();
			}).findFirst().isPresent();
	}
	
	public boolean updateActors(String alias, ActorFactory factory) {
		return updateActors(alias, factory, 1);
	}
	
	public boolean updateActors(String alias, ActorFactory factory, int instances) {
		boolean result = false;
		if (instances>0) {
			List<UUID> oldActors = getActorsFromAlias(alias);
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
			
			List<UUID> newActors = addActor(factory, instances);
			setAlias(newActors, alias);
			if (oldActors.size()>0)
				broadcast(ActorMessage.create(null, Actor.STOP, SYSTEM_ID, null), new ActorGroupSet(oldActors));
			result = true;
		}
		
		return result;
	}
	
	@Override
	public void removeActor(UUID id) {	
		cells.remove(id);
		resourceCells.remove(id);
		podCells.remove(id);
		pseudoCells.remove(id);
		
		container.unregister(id);
		
		String alias = null;
		if ((alias=hasAliases.get(id))!=null) {
			hasAliases.remove(id);
			Queue<UUID> queue = aliases.get(alias);
			queue.remove(id);
			if (queue.isEmpty())
				aliases.remove(alias);
		}
	}
	
	@Override
	public boolean hasActor(String uuid) {
		UUID key;
		try {
			key = UUID.fromString(uuid);
		}
		catch (IllegalArgumentException e) {
			return false;
		}
		return cells.containsKey(key);
	}		
	
	@Override
	public ActorSystemImpl setAlias(UUID id, String alias) {
		if (id!=null && alias!=null && !alias.isEmpty()) {
			Queue<UUID> queue = null;
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
	public ActorSystemImpl setAlias(List<UUID> ids, String alias) {
		for (UUID id : ids)
			setAlias(id, alias);
		
		return this;
	}
	
	@Override
	public UUID getActorFromAlias(String alias) {
		List<UUID> result = getActorsFromAlias(alias);
		
		return !result.isEmpty() ? result.get(0) : null;
	}
	
	@Override
	public List<UUID> getActorsFromAlias(String alias) {
		List<UUID> result = new LinkedList<>();
		
		Queue<UUID> queue = aliases.get(alias);
		if (queue!=null)
			queue.forEach((id) -> result.add(id));
		
		return result;
	}
	
	@Override
	public String getActorPath(UUID uuid) {
		String result = null;
		
		if (uuid!=null) {
			if (uuid.equals(USER_ID))
				result = "/";
			else {
				StringBuffer buffer = new StringBuffer();
				InternalActorCell cell = cells.get(uuid);
				if (cell.getActor()!=null)
					buffer.append("/" + (cell.getActor().getName()!=null ? cell.getActor().getName():cell.getActor().getId().toString()));
				UUID parent = null;
				while ((parent=cell.getParent())!=null && !parent.equals(USER_ID)) {
					cell = cells.get(parent);
					buffer.insert(0, "/" + (cell.getActor().getName()!=null ? cell.getActor().getName():cell.getActor().getId().toString()));
				}
				
				result = buffer.toString();
			}
		}
		
		return result;
	}
	
	@Override
	public UUID getActorFromPath(String path) {
		InternalActorCell result = null;
		
		if (path!=null) {
			InternalActorCell parent = cells.get(USER_ID);
			
			if (path.isEmpty() || path.equals("/"))
				result = parent;
			else {
				StringTokenizer tokenizer = new StringTokenizer(path, "/");
				String token = null;
				while (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					
					Iterator<UUID> iterator = parent.getChildren().iterator();
					result  = null;
					while (iterator.hasNext()) {
						InternalActorCell child = cells.get(iterator.next());
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
		UUID dest = getActorFromPath(path);
		if (dest!=null)
			send(message.shallowCopy(dest));
		
		return this;
	}
	
	@Override
	public ActorSystemImpl sendViaAlias(ActorMessage<?> message, String alias) {
		List<UUID> destinations = getActorsFromAlias(alias);
		
		if (!destinations.isEmpty()) {
			UUID dest = null;
			
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
		
		List<UUID> destinations = getActorsFromAlias(alias);
		if (!destinations.isEmpty()) {
			UUID dest = null;
			
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
		if (executerService.isStarted() && messagingEnabled.get() && message!=null && message.dest()!=null)  {
			InternalActorCell cell = cells.get(message.dest());
			if (cell.isActive())
				messageDispatcher.postOuter(message);
			else
				((ActorTimerExecuterService)executerService.globalTimer()).schedule(new Runnable() {
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
		if (!messagingEnabled.get()) 
			bufferQueue.offer(message.copy());
		else
			messageDispatcher.postServer(message);
	}
	
	@Override
	public void sendAsDirective(ActorMessage<?> message) {
		if (messagingEnabled.get()) 
			messageDispatcher.postDirective(message);
	}
	
	@Override
	public ActorSystemImpl broadcast(ActorMessage<?> message, ActorGroup group) {
		if (!messagingEnabled.get())
			for (UUID id : group)
				bufferQueue.offer(message.copy(id));
		else
			for (UUID id : group)
				messageDispatcher.postOuter(message.shallowCopy(id));
		
		return this;
	}
	
	@Override
	public UUID getRedirectionDestination(UUID source) {
		return redirector.get(source);
	}
	
	@Override
	public ActorSystemImpl addRedirection(UUID source, UUID dest) {
		redirector.put(source, dest);
		
		return this;
	}
	
	@Override
	public ActorSystemImpl removeRedirection(UUID source) {
		redirector.remove(source);
		
		return this;
	}
	
	@Override
	public ActorSystemImpl clearRedirections() {
		redirector.clear();
		
		return this;
	}
	
	@Override
	public ActorTimer timer() {
		return executerService.timer();
	}
	
	@Override
	public ActorTimer globalTimer() {
		return executerService.globalTimer();
	}
	
	@Override
	public boolean start() {
		return start(null, null);
	}
	
	@Override
	public boolean start(Runnable onStartup, Runnable onTermination) {
		boolean result = false;
		
		if (!executerService.isStarted()) {
			executerService.start(new Runnable() {
				@Override
				public void run() {
					/* preStart */
					Iterator<Entry<UUID, InternalActorCell>> iterator = cells.entrySet().iterator();
					while (iterator.hasNext()) {
						InternalActorCell cell = iterator.next().getValue();
						if (cell.isRootInUser() || cell.isRootInSystem() )
							cell.preStart();
					}
					
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
	public void shutdownWithActors() {
		shutdownWithActors(false);
	}
	
	@Override
	public void shutdownWithActors(final boolean await) {
		if (executerService.isStarted()) {
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
					messageDispatcher.unregisterCell(cells.get(PSEUDO_ID));
					removeActor(PSEUDO_ID);
					
					executerService.shutdown(await);
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
		if (executerService.isStarted())
			executerService.shutdown(await);
	}
}
