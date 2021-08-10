/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package io.actor4j.core.internal;

import static io.actor4j.core.internal.protocols.ActorProtocolTag.*;

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

import io.actor4j.core.ActorPodService;
import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.PseudoActor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.internal.di.DIContainer;
import io.actor4j.core.internal.di.DefaultDIContainer;
import io.actor4j.core.internal.pods.PodReplicationController;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.PodFactory;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.core.utils.ActorTimer;
import io.actor4j.core.utils.PodActorFactory;

public abstract class ActorSystemImpl implements ActorPodService {
	protected final ActorSystem wrapper;
	
	protected /*Changeable only before starting*/ ActorSystemConfig config;
	
	protected /*quasi final*/ DIContainer<UUID> container;
	protected /*quasi final*/ PodReplicationController podReplicationController;
	protected /*quasi final*/ PodReplicationControllerRunnableFactory podReplicationControllerRunnableFactory;
	
	protected final Map<UUID, ActorCell> cells; // ActorCellID    -> ActorCell
	protected final Map<String, Queue<UUID>> aliases;  // ActorCellAlias -> ActorCellID
	protected final Map<UUID, String> hasAliases;
	protected final Map<UUID, Boolean> resourceCells;
	protected final Map<UUID, Boolean> podCells;
	protected final Map<String, Queue<UUID>> podDomains; // PodActorCellDomain -> ActorCellID
	protected final Map<UUID, ActorCell> pseudoCells;
	protected final Map<UUID, UUID> redirector;
	protected /*quasi final*/ ActorMessageDispatcher messageDispatcher;
	protected /*quasi final*/ ActorThreadFactory actorThreadFactory;
	
	protected final AtomicBoolean messagingEnabled;
	
	protected final Queue<ActorMessage<?>> bufferQueue;
	protected final ActorExecuterService executerService;
	
	protected final ActorStrategyOnFailure actorStrategyOnFailure;
	
	protected CountDownLatch countDownLatch;
	
	public UUID USER_ID;
	public final UUID SYSTEM_ID;
	public final UUID UNKNOWN_ID;
	
	public ActorSystemImpl(ActorSystem wrapper) {
		this(wrapper, null);
	}
	
	public ActorSystemImpl(ActorSystem wrapper, ActorSystemConfig config) {
		super();
		
		this.wrapper = wrapper;
		if (config!=null)
			this.config = config;
		else
			this.config = ActorSystemConfig.create();
		
		container = DefaultDIContainer.create();
		podReplicationController = new PodReplicationController(this);
		podReplicationControllerRunnableFactory = (system) -> new DefaultPodReplicationControllerRunnable(system);
		
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
		executerService = new ActorExecuterService(this);
		
		actorStrategyOnFailure = new ActorStrategyOnFailure(this);
				
		resetUserCell();
		
		SYSTEM_ID = internal_addCell(generateCell(new Actor("system") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		}));
		UNKNOWN_ID = internal_addCell(generateCell(new Actor("unknown") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		}));
	}
	
	protected void reset() {
		messagingEnabled.set(false);
		
		aliases.clear();
		hasAliases.clear();
		resourceCells.clear();
		pseudoCells.clear();
		redirector.clear();
		
		bufferQueue.clear();
		
		resetUserCell();
	}
	
	protected void resetUserCell() {
		countDownLatch = new CountDownLatch(1);
		
		USER_ID = internal_addCell(generateCell(new Actor("user") {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
			
			@Override
			public void postStop() {
				countDownLatch.countDown();
			}
		}));
	}
	
	public ActorCell generateCell(Actor actor) {
		if (actor instanceof ResourceActor)
			return new ResourceActorCell(this, actor);
		else if (actor instanceof PodActor)
			return new PodActorCell(this, actor);
		else
			return new ActorCell(this, actor);
	}
	
	public ActorCell generateCell(Class<? extends Actor> clazz) {
		if (clazz==ResourceActor.class)
			return new ResourceActorCell(this, null);
		else if (clazz==PodActor.class)
			return new PodActorCell(this, null);
		else
			return new ActorCell(this, null);
	}

	public ActorSystemConfig getConfig() {
		return config;
	}
	
	public boolean setConfig(ActorSystemConfig config) {
		boolean result = false;
		
		if (!executerService.isStarted()) {
			this.config = config;
			result = true;
		}
		
		return result;
	}

	public DIContainer<UUID> getContainer() {
		return container;
	}
	
	public PodReplicationController getPodReplicationController() {
		return podReplicationController;
	}

	public Map<UUID, ActorCell> getCells() {
		return cells;
	}
	
	public Map<UUID, ActorCell> getPseudoCells() {
		return pseudoCells;
	}
	
	public Map<UUID, Boolean> getResourceCells() {
		return resourceCells;
	}
	
	public Map<UUID, Boolean> getPodCells() {
		return podCells;
	}

	public Map<String, Queue<UUID>> getPodDomains() {
		return podDomains;
	}

	public Map<String, Queue<UUID>> getAliases() {
		return aliases;
	}
	
	public Map<UUID, UUID> getRedirector() {
		return redirector;
	}

	public ActorMessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}
	
	public ActorThreadFactory getActorThreadFactory() {
		return actorThreadFactory;
	}

	public void setActorThreadFactory(ActorThreadFactory actorThreadFactory) {
		this.actorThreadFactory = actorThreadFactory;
	}

	public ActorStrategyOnFailure getActorStrategyOnFailure() {
		return actorStrategyOnFailure;
	}

	protected UUID internal_addCell(ActorCell cell) {
		Actor actor = cell.actor;
		if (actor instanceof PseudoActor)
			pseudoCells.put(cell.id, cell);
		else {
			actor.setCell(cell);
			cells.put(cell.id, cell);
			if (actor instanceof ResourceActor)
				resourceCells.put(cell.id, false);
			else if (actor instanceof PodActor)
				podCells.put(cell.id, false);
			if (executerService.isStarted()) {
				messageDispatcher.registerCell(cell);
				/* preStart */
				cell.preStart();
			}
		}
		return cell.id;
	}
	
	protected UUID user_addCell(ActorCell cell) {
		cell.parent = USER_ID;
		cells.get(USER_ID).children.add(cell.id);
		return internal_addCell(cell);
	}
	
	protected UUID system_addCell(ActorCell cell) {
		cell.parent = SYSTEM_ID;
		cells.get(SYSTEM_ID).children.add(cell.id);
		return internal_addCell(cell);
	}

	public UUID addActor(ActorFactory factory) {
		ActorCell cell = generateCell(factory.create());
		container.register(cell.id, factory);
		
		return user_addCell(cell);
	}
	
	public List<UUID> addActor(ActorFactory factory, int instances) {
		List<UUID> result = new ArrayList<>(instances);
		
		for (int i=0; i<instances; i++)
			result.add(addActor(factory));
			
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
		PodActorCell cell = (PodActorCell)generateCell(factory.create());
		cell.context = context;
		setPodDomain(cell.id, context.getDomain());
		container.register(cell.id, factory);
		
		return user_addCell(cell);
	}
	
	public void deployPods(File jarFile, PodConfiguration podConfiguration) {
		podReplicationController.deployPods(jarFile, podConfiguration);
	}
	
	public void deployPods(PodFactory factory, PodConfiguration podConfiguration) {
		podReplicationController.deployPods(factory, podConfiguration);
	}
	
	public void undeployPods(String domain) {
		podReplicationController.undeployPods(domain);
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
				broadcast(new ActorMessage<Object>(null, Actor.STOP, SYSTEM_ID, null), new ActorGroupSet(oldActors));
			result = true;
		}
		
		return result;
	}
	
	protected void removeActor(UUID id) {	
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
	
	public ActorSystemImpl setAlias(List<UUID> ids, String alias) {
		for (UUID id : ids)
			setAlias(id, alias);
		
		return this;
	}
	
	public UUID getActorFromAlias(String alias) {
		List<UUID> result = getActorsFromAlias(alias);
		
		return !result.isEmpty() ? result.get(0) : null;
	}
	
	public List<UUID> getActorsFromAlias(String alias) {
		List<UUID> result = new LinkedList<>();
		
		Queue<UUID> queue = aliases.get(alias);
		if (queue!=null)
			queue.forEach((id) -> result.add(id));
		
		return result;
	}
	
	public String getActorPath(UUID uuid) {
		String result = null;
		
		if (uuid!=null) {
			if (uuid.equals(USER_ID))
				result = "/";
			else {
				StringBuffer buffer = new StringBuffer();
				ActorCell cell = cells.get(uuid);
				if (cell.getActor()!=null)
					buffer.append("/" + (cell.getActor().getName()!=null ? cell.getActor().getName():cell.getActor().getId().toString()));
				UUID parent = null;
				while ((parent=cell.parent)!=null && !parent.equals(USER_ID)) {
					cell = cells.get(parent);
					buffer.insert(0, "/" + (cell.getActor().getName()!=null ? cell.getActor().getName():cell.getActor().getId().toString()));
				}
				
				result = buffer.toString();
			}
		}
		
		return result;
	}
	
	public UUID getActorFromPath(String path) {
		ActorCell result = null;
		
		if (path!=null) {
			ActorCell parent = cells.get(USER_ID);
			
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
						ActorCell child = cells.get(iterator.next());
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
	
	public ActorSystemImpl send(ActorMessage<?> message) {
		if (!messagingEnabled.get()) 
			bufferQueue.offer(message.copy());
		else
			messageDispatcher.postOuter(message);
		
		return this;
	}
	
	public ActorSystemImpl sendViaPath(ActorMessage<?> message, String path) {
		message.dest = getActorFromPath(path);
		if (message.dest!=null)
			send(message);
		
		return this;
	}
	
	public ActorSystemImpl sendViaAlias(ActorMessage<?> message, String alias) {
		List<UUID> destinations = getActorsFromAlias(alias);
		
		if (!destinations.isEmpty()) {
			if (destinations.size()==1)
				message.dest = destinations.get(0);
			else 
				message.dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			if (message.dest!=null)
				send(message);
		}
		
		return this;
	}
	
	public boolean sendViaAliasAsServer(ActorMessage<?> message, String alias) {
		boolean result = false;
		
		List<UUID> destinations = getActorsFromAlias(alias);
		if (!destinations.isEmpty()) {
			if (destinations.size()==1)
				message.dest = destinations.get(0);
			else 
				message.dest = destinations.get(ThreadLocalRandom.current().nextInt(destinations.size()));
			if (message.dest!=null) {
				sendAsServer(message);
				result = true;
			}
		}
		
		return result;
	}
	
	public ActorSystemImpl sendWhenActive(ActorMessage<?> message) {
		if (executerService.isStarted() && messagingEnabled.get() && message!=null && message.dest!=null)  {
			ActorCell cell = cells.get(message.dest);
			if (cell.isActive())
				messageDispatcher.postOuter(message);
			else
				executerService.globalTimerExecuterService.schedule(new Runnable() {
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
	
	public void sendAsServer(ActorMessage<?> message) {
		if (!messagingEnabled.get()) 
			bufferQueue.offer(message.copy());
		else
			messageDispatcher.postServer(message);
	}
	
	public void sendAsDirective(ActorMessage<?> message) {
		if (messagingEnabled.get()) 
			messageDispatcher.postDirective(message);
	}
	
	public ActorSystemImpl broadcast(ActorMessage<?> message, ActorGroup group) {
		if (!messagingEnabled.get())
			for (UUID id : group) {
				message.dest = id;
				bufferQueue.offer(message.copy());
			}
		else
			for (UUID id : group) {
				message.dest = id;
				messageDispatcher.postOuter(message);
			}
		
		return this;
	}
	
	public UUID getRedirectionDestination(UUID source) {
		return redirector.get(source);
	}
	
	public void addRedirection(UUID source, UUID dest) {
		redirector.put(source, dest);
	}
	
	public void removeRedirection(UUID source) {
		redirector.remove(source);
	}
	
	public void clearRedirections() {
		redirector.clear();
	}
	
	public ActorTimer timer() {
		return executerService.timer();
	}
	
	public ActorTimer globalTimer() {
		return executerService.globalTimer();
	}
	
	public void start() {
		start(null, null);
	}
	
	public void start(Runnable onStartup, Runnable onTermination) {
		if (!executerService.isStarted())
			executerService.start(new Runnable() {
				@Override
				public void run() {
					/* preStart */
					Iterator<Entry<UUID, ActorCell>> iterator = cells.entrySet().iterator();
					while (iterator.hasNext()) {
						ActorCell cell = iterator.next().getValue();
						if (cell.isRootInUser() /*&& cell.isRootInSystem()*/ )
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
	}
	
	public void shutdownWithActors() {
		shutdownWithActors(false);
	}
	
	public void shutdownWithActors(final boolean await) {
		if (executerService.isStarted()) {
			Thread waitOnTermination = new Thread(new Runnable() {
				@Override
				public void run() {
					send(new ActorMessage<>(null, INTERNAL_STOP, USER_ID, USER_ID));
					try {
						countDownLatch.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					executerService.shutdown(await);

					reset();
				}
			});
			
			waitOnTermination.start();
			if (await)
				try {
					waitOnTermination.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
		}
	}
	
	public void shutdown() {
		shutdown(false);
	}
	
	public void shutdown(boolean await) {
		if (executerService.isStarted())
			executerService.shutdown(await);
	}
	
	public ActorExecuterService getExecuterService() {
		return executerService;
	}
}
