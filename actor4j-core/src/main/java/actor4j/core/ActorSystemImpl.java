/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import actor4j.core.actors.Actor;
import actor4j.core.actors.PseudoActor;
import actor4j.core.actors.ResourceActor;
import actor4j.core.balancing.ActorBalancingOnCreation;
import actor4j.core.balancing.ActorBalancingOnRuntime;
import actor4j.core.exceptions.ActorInitializationException;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.utils.ActorGroup;
import safety4j.SafetyManager;
import tools4j.di.DIContainer;
import tools4j.di.InjectorParam;

import static actor4j.core.protocols.ActorProtocolTag.*;
import static actor4j.core.utils.ActorUtils.*;

public abstract class ActorSystemImpl {
	protected ActorSystem wrapper;
	
	protected String name;
	
	protected DIContainer<UUID> container;
	
	protected Map<UUID, ActorCell> cells; // ActorCellID    -> ActorCell
	protected Map<String, UUID> aliases;  // ActorCellAlias -> ActorCellID
	protected Map<UUID, String> hasAliases;
	protected Map<UUID, Boolean> resourceCells;
	protected Map<UUID, ActorCell> pseudoCells;
	protected ActorMessageDispatcher messageDispatcher;
	protected Class<? extends ActorThread> actorThreadClass;
	
	protected boolean counterEnabled;
	
	protected int parallelismMin;
	protected int parallelismFactor;
	
	protected int idle;
	protected boolean softMode; // hard, soft
	protected long softSleep;
	
	protected boolean debugUnhandled;
	
	protected int queueSize;
	protected int bufferQueueSize;
	
	protected int throughput;
	
	protected Queue<ActorMessage<?>> bufferQueue;
	protected ActorExecuterService executerService;
	
	protected ActorBalancingOnCreation actorBalancingOnCreation;
	protected ActorBalancingOnRuntime actorBalancingOnRuntime;
	
	protected ActorStrategyOnFailure actorStrategyOnFailure;
	
	protected List<ActorServiceNode> serviceNodes;
	protected boolean clientMode;
	protected ActorClientRunnable clientRunnable;
	
	protected CountDownLatch countDownLatch;
	
	public final UUID USER_ID;
	public final UUID SYSTEM_ID;
	public final UUID UNKNOWN_ID;
	
	public ActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public ActorSystemImpl(String name, ActorSystem wrapper) {
		super();
		
		if (name!=null)
			this.name = name;
		else
			this.name = "actor4j";
		
		this.wrapper = wrapper;
		
		container      = DIContainer.create();
		
		cells          = new ConcurrentHashMap<>();
		aliases        = new ConcurrentHashMap<>();
		hasAliases     = new ConcurrentHashMap<>();
		resourceCells  = new ConcurrentHashMap<>();
		pseudoCells    = new ConcurrentHashMap<>();
		
		setParallelismMin(0);
		parallelismFactor = 1;
		
		idle = 100000;
		softMode  = true;
		softSleep = 25;
		
		queueSize       = 50000;
		bufferQueueSize = 10000;
		
		throughput = 100;
		
		bufferQueue = new ConcurrentLinkedQueue<>();
		executerService = new ActorExecuterService(this);
		
		actorBalancingOnCreation = new ActorBalancingOnCreation();
		actorBalancingOnRuntime = new ActorBalancingOnRuntime();
		
		actorStrategyOnFailure = new ActorStrategyOnFailure(this);
		
		serviceNodes = new ArrayList<>();
				
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
	
	public ActorCell generateCell(Actor actor) {
		if (actor instanceof ResourceActor)
			return new ResourceActorCell(this, actor);
		else
			return new ActorCell(this, actor);
	}
	
	public ActorCell generateCell(Class<? extends Actor> clazz) {
		if (clazz==ResourceActor.class)
			return new ResourceActorCell(this, null);
		else
			return new ActorCell(this, null);
	}

	public String getName() {
		return name;
	}
	
	public DIContainer<UUID> getContainer() {
		return container;
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
	
	public Map<String, UUID> getAliases() {
		return aliases;
	}

	public ActorMessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	public ActorStrategyOnFailure getActorStrategyOnFailure() {
		return actorStrategyOnFailure;
	}

	public boolean isClientMode() {
		return clientMode;
	}

	public ActorSystemImpl setClientRunnable(ActorClientRunnable clientRunnable) {
		clientMode = (clientRunnable!=null);
			
		this.clientRunnable = clientRunnable;
		
		return this;
	}
	
	public boolean isCounterEnabled() {
		return counterEnabled;
	}

	public void setCounterEnabled(boolean counterEnabled) {
		this.counterEnabled = counterEnabled;
	}

	public int getParallelismMin() {
		return parallelismMin;
	}
	
	public ActorSystemImpl setParallelismMin(int parallelismMin) {
		if (parallelismMin<=0)
			this.parallelismMin = Runtime.getRuntime().availableProcessors();
		else
			this.parallelismMin = parallelismMin;
		
		return this;
	}

	public int getParallelismFactor() {
		return parallelismFactor;
	}
	
	public ActorSystemImpl setParallelismFactor(int parallelismFactor) {
		this.parallelismFactor = parallelismFactor;
		
		return this;
	}
	
	public boolean isSoftMode() {
		return softMode;
	}
	
	public void setSoftMode(boolean softMode, long softSleep) {
		this.softMode = softMode;
		this.softSleep = softSleep;
	}
	
	public long getSoftSleep() {
		return softSleep;
	}

	public ActorSystemImpl softMode() {
		this.softMode = true;
		
		return this;
	}
	
	public ActorSystemImpl hardMode() {
		this.softMode = false;
		
		return this;
	}
	
	public int getQueueSize() {
		return queueSize;
	}

	public int getBufferQueueSize() {
		return bufferQueueSize;
	}
	
	public void setBufferQueueSize(int bufferQueueSize) {
		this.bufferQueueSize = bufferQueueSize;
	}
	
	public int getThroughput() {
		return throughput;
	}

	public void setThroughput(int throughput) {
		this.throughput = throughput;
	}

	public ActorSystemImpl setDebugUnhandled(boolean debugUnhandled) {
		this.debugUnhandled = debugUnhandled;
		
		return this;
	}
		
	public ActorSystemImpl addServiceNode(ActorServiceNode serviceNode) {
		serviceNodes.add(serviceNode);
		
		return this;
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
	
	public UUID addActor(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
		InjectorParam[] params = new InjectorParam[args.length];
		for (int i=0; i<args.length; i++)
			params[i] = InjectorParam.createWithObj(args[i]);
		
		ActorCell cell = generateCell(clazz);
		container.registerConstructorInjector(cell.id, clazz, params);
		Actor actor = null;
		try {
			actor = (Actor)container.getInstance(cell.id);
			cell.actor = actor;
		} catch (Exception e) {
			e.printStackTrace();
			SafetyManager.getInstance().notifyErrorHandler(new ActorInitializationException(), "initialization", null);
		}
		
		return (actor!=null) ? user_addCell(cell) : UUID_ZERO;
	}
	
	public UUID addActor(ActorFactory factory) {
		ActorCell cell = generateCell(factory.create());
		container.registerFactoryInjector(cell.id, factory);
		
		return user_addCell(cell);
	}
	
	protected void removeActor(UUID id) {	
		cells.remove(id);
		resourceCells.remove(id);
		pseudoCells.remove(id);
		
		container.unregister(id);
		
		String alias = null;
		if ((alias=hasAliases.get(id))!=null) {
			hasAliases.remove(id);
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
		if (!hasAliases.containsKey(id)) {
			aliases.put(alias, id);
			hasAliases.put(id, alias);
		}	
		else {
			aliases.remove(hasAliases.get(id));
			aliases.put(alias, id);
			hasAliases.put(id, alias);
		}
		
		return this;
	}
	
	public UUID getActor(String alias) {
		return aliases.get(alias);
	}
	
	public ActorSystemImpl send(ActorMessage<?> message) {
		if (!executerService.isStarted()) 
			bufferQueue.offer(message.copy());
		else
			messageDispatcher.postOuter(message);
		
		return this;
	}
	
	public void sendAsServer(ActorMessage<?> message) {
		if (!executerService.isStarted()) 
			bufferQueue.offer(message.copy());
		else
			messageDispatcher.postServer(message);
	}
	
	public void sendAsDirective(ActorMessage<?> message) {
		if (executerService.isStarted()) 
			messageDispatcher.postDirective(message);
	}
	
	public ActorSystemImpl broadcast(ActorMessage<?> message, ActorGroup group) {
		if (!executerService.isStarted())
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
	
	public ActorTimer timer() {
		return executerService.timer();
	}
	
	public void start() {
		start(null);
	}
	
	public void start(Runnable onTermination) {
		if (!executerService.isStarted())
			executerService.start(new Runnable() {
				@Override
				public void run() {
					/* preStart */
					for (ActorCell cell : cells.values())
						cell.preStart();
					
					executerService.started.set(true);
					
					ActorMessage<?> message = null;
					while ((message=bufferQueue.poll())!=null)
						messageDispatcher.postOuter(message);
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
	
	public List<ActorServiceNode> getServiceNodes() {
		return serviceNodes;
	}
}
