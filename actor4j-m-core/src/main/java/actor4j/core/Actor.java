/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import actor4j.core.actor.protocols.StopProtocol;
import actor4j.core.supervisor.DefaultSupervisiorStrategy;
import actor4j.core.supervisor.SupervisorStrategy;
import actor4j.function.Consumer;
import actor4j.function.Predicate;
import tools4j.di.InjectorParam;

import static actor4j.core.ActorLogger.logger;
import static actor4j.core.ActorUtils.actorLabel;
import static actor4j.core.ActorUtils.*;
import static actor4j.core.actor.protocols.ActorProtocolTag.*;

public abstract class Actor {
	protected ActorSystem system;
	
	protected UUID id;
	protected String name;
	
	protected UUID parent;
	protected Queue<UUID> children;
	
	protected Deque<Consumer<ActorMessage<?>>> behaviourStack;
	
	protected Queue<ActorMessage<?>> stash; //must be initialized by hand
	
	protected StopProtocol stopProtocol;
	
	protected Queue<UUID> deathWatcher;
	
	public static final int POISONPILL = INTERNAL_STOP;
	public static final int TERMINATED = INTERNAL_STOP_SUCCESS;
			
	public Actor() {
		this(null);
	}
	
	public Actor(String name) {
		super();
		
		this.id   = UUID.randomUUID();
		this.name = name;
		
		children = new ConcurrentLinkedQueue<>();
		
		behaviourStack = new ArrayDeque<>();
		
		stopProtocol = new StopProtocol(this);
		
		deathWatcher =  new ConcurrentLinkedQueue<>();
	}
	
	protected void setSystem(ActorSystem system) {
		this.system = system;
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID getSelf() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public UUID getParent() {
		return parent;
	}
	
	public Queue<UUID> getChildren() {
		return children;
	}
	
	public boolean isRoot() {
		return (parent==null);
	}
	
	public boolean isRootInUser() {
		return (parent==system.USER_ID);
	}
	
	protected void internal_receive(ActorMessage<?> message) {
		if (message.tag==POISONPILL)
			stop();
		else {
			Consumer<ActorMessage<?>> behaviour = behaviourStack.peek();
			if (behaviour==null)
				receive(message);
			else
				behaviour.accept(message);
		}
	}
	
	protected abstract void receive(ActorMessage<?> message);
	
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		if (replace && !behaviourStack.isEmpty())
			behaviourStack.pop();
		behaviourStack.push(behaviour);
	}
	
	public void become(Consumer<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	public void unbecome() {
		behaviourStack.pop();
	}
	
	public void unbecomeAll() {
		behaviourStack.clear();
	}
	
	public void await(final UUID source, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.source.equals(source)) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void await(final int tag, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.tag==tag) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void await(final UUID source, final int tag, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.source.equals(source) && message.tag==tag) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (predicate.test(message)) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void send(ActorMessage<?> message) {
		system.messageDispatcher.post(message);
	}
	
	public void send(ActorMessage<?> message, String alias) {
		system.messageDispatcher.post(message, alias);
	}
	
	public void send(ActorMessage<?> message, UUID dest) {
		message.source = id;
		message.dest   = dest;
		send(message);
	}
	
	public void forward(ActorMessage<?> message, UUID dest) {
		message.dest   = dest;
		send(message);
	}
	
	public void unhandled(ActorMessage<?> message) {
		if (system.debugUnhandled) {
			Actor sourceActor = system.actors.get(message.source);
			if (sourceActor!=null)
				logger().warn(
					String.format("System - actor (%s): Unhandled message (%s) from source (%s)",
						actorLabel(this), message.toString(), actorLabel(sourceActor)
					));
			else
				logger().warn(
					String.format("System - actor (%s): Unhandled message (%s) from unavaible source (???)",
						actorLabel(this), message.toString()
					));
		}
	}
	
	public void setAlias(String alias) {
		system.setAlias(id, alias);
	}
	
	protected UUID internal_addChild(Actor actor) {
		actor.parent = id;
		children.add(actor.getId());
		system.system_addActor(actor);
		system.messageDispatcher.registerActor(actor);
		/* preStart */
		actor.preStart();
		
		return actor.getId();
	}
	
	public UUID addChild(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
		InjectorParam[] params = new InjectorParam[args.length];
		for (int i=0; i<args.length; i++)
			params[i] = InjectorParam.createWithObj(args[i]);
		
		UUID temp = UUID.randomUUID();
		system.container.registerConstructorInjector(temp, clazz, params);
		
		Actor actor = null;
		try {
			actor = (Actor)system.container.getInstance(temp);
			system.container.registerConstructorInjector(actor.getId(), clazz, params);
			system.container.unregister(temp);
		} catch (Exception e) {
			throw new ActorInitializationException();
		}
		
		return (actor!=null) ? internal_addChild(actor) : UUID_ZERO;
	}
	
	public UUID addChild(ActorFactory factory) {
		Actor actor = factory.create();
		system.container.registerFactoryInjector(actor.getId(), factory);
		
		return internal_addChild(actor);
	}
	
	public SupervisorStrategy supervisorStrategy() {
		return new DefaultSupervisiorStrategy();
	}
	
	public void preStart() {
		// empty
	}
	
	public void preRestart(Exception reason) {
		stopProtocol.apply(false);
	}
	
	public void postRestart(Exception reason) {
		preStart();
	}
	
	public void postStop() {
		// empty
	}
	
	public void stop() {
		stopProtocol.apply(true);
	}
	
	public void internal_stop() {
		if (parent!=null)
			system.actors.get(parent).children.remove(getSelf());
		system.messageDispatcher.unregisterActor(this);
		system.removeActor(id);
		Iterator<UUID> iterator = deathWatcher.iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			send(new ActorMessage<>(null, TERMINATED, getSelf(), dest));
		}
	}
	
	public void watch(UUID dest) {
		Actor actor = system.actors.get(dest);
		if (actor!=null)
			actor.deathWatcher.add(getSelf());
	}
	
	public void unwatch(UUID dest) {
		Actor actor = system.actors.get(dest);
		if (actor!=null)
			actor.deathWatcher.remove(getSelf());
	}
}
