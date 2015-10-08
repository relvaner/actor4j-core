/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import actor4j.function.Consumer;
import actor4j.function.Predicate;
import actor4j.supervisor.SupervisorStrategy;

import static actor4j.core.ActorLogger.logger;
import static actor4j.core.ActorUtils.actorLabel;

public abstract class Actor {
	protected ActorSystem system;
	
	protected UUID id;
	protected String name;
	
	protected UUID parent;
	protected Queue<UUID> children;
	
	protected Deque<Consumer<ActorMessage<?>>> behaviourStack;
			
	public Actor() {
		this(null);
	}
	
	public Actor(String name) {
		super();
		
		this.id = UUID.randomUUID();
		this.name = name;
		
		children = new ConcurrentLinkedQueue<>();
		
		behaviourStack = new ArrayDeque<>();
	}
	
	protected void setSystem(ActorSystem system) {
		this.system = system;
	}
	
	public UUID getId() {
		return id;
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
		Consumer<ActorMessage<?>> behaviour = behaviourStack.peek();
		if (behaviour==null)
			receive(message);
		else
			behaviour.accept(message);
	}
	
	protected abstract void receive(ActorMessage<?> message);
	
	protected void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		if (replace && !behaviourStack.isEmpty())
			behaviourStack.pop();
		behaviourStack.push(behaviour);
	}
	
	protected void become(Consumer<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	protected void unbecome() {
		behaviourStack.pop();
	}
	
	protected void unbecomeAll() {
		behaviourStack.clear();
	}
	
	protected void await(UUID source, Consumer<ActorMessage<?>> action) {
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
	
	protected void await(int tag, Consumer<ActorMessage<?>> action) {
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
	
	protected void await(UUID source, int tag, Consumer<ActorMessage<?>> action) {
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
	
	protected void await(Predicate<ActorMessage<?>> predicate, Consumer<ActorMessage<?>> action) {
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
	
	protected void send(ActorMessage<?> message) {
		system.messagePassing.post(message);
	}
	
	protected void send(ActorMessage<?> message, String alias) {
		system.messagePassing.post(message, alias);
	}
	
	protected void send(ActorMessage<?> message, UUID dest) {
		message.source = id;
		message.dest   = dest;
		send(message);
	}
	
	protected void forward(ActorMessage<?> message, UUID dest) {
		message.dest   = dest;
		send(message);
	}
	
	protected void unhandled(ActorMessage<?> message) {
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
	
	protected void setAlias(String alias) {
		system.setAlias(id, alias);
	}
	
	protected UUID addChild(Actor actor) {
		actor.parent = id;
		children.add(actor.getId());
		system.addActor(actor);
		system.messagePassing.registerActor(actor);
		
		return actor.getId();
	}
	
	protected SupervisorStrategy supervisorStrategy() {
		return null;
	}
	
	protected void preStart() {
		// empty
	}
	
	protected void preRestart() {
		// empty
	}
	
	protected void postRestart() {
		// empty
	}
	
	protected void postStop() {
		// empty
	}
	
	protected void stop() {
		if (parent!=null)
			system.actors.get(parent).children.remove(parent);
		system.messagePassing.unregisterActor(this);
		system.removeActor(id);
		postStop();
	}
}
