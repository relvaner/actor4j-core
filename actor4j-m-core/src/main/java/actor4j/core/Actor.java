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

import actor4j.core.exceptions.ActorInitializationException;
import actor4j.core.exceptions.ActorKilledException;
import actor4j.core.messages.ActorMessage;
import actor4j.core.supervisor.DefaultSupervisiorStrategy;
import actor4j.core.supervisor.SupervisorStrategy;
import actor4j.core.utils.ActorFactory;
import actor4j.function.Consumer;
import actor4j.function.Function;
import actor4j.function.Predicate;
import tools4j.di.InjectorParam;

import static actor4j.core.ActorProtocolTag.*;
import static actor4j.core.utils.ActorLogger.logger;
import static actor4j.core.utils.ActorUtils.*;

public abstract class Actor {
	protected ActorCell cell;
	
	protected String name;
	
	protected Queue<ActorMessage<?>> stash; //must be initialized by hand
	
	public static final int POISONPILL = INTERNAL_STOP;
	public static final int TERMINATED = INTERNAL_STOP_SUCCESS;
	public static final int KILL       = INTERNAL_KILL;
	
	public static final int STOP       = INTERNAL_STOP;
	public static final int RESTART    = INTERNAL_RESTART;
	
	/**
	 * Don't create here, new actors as child or send messages too other actors. You will 
	 * get a NullPointerException, because the variable system is not initialized. It will 
	 * injected later by the framework. Use instead the method preStart for these reasons.
	 */
	public Actor() {
		this(null);
	}
	
	/**
	 * Don't create here, new actors as child or send messages too other actors. You will 
	 * get a NullPointerException, because the variable system is not initialized. It will 
	 * injected later by the framework. Use instead the method preStart for these reasons.
	 */
	public Actor(String name) {
		super();
		
		this.name = name;
	}
	
	public UUID self() {
		return cell.id;
	}
	
	public boolean isRoot() {
		
	}
	
	public boolean isRootInUser() {
		
	}
	
	public abstract void receive(ActorMessage<?> message);
	
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		
	}
	
	public void become(Consumer<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	public void unbecome() {
		
	}
	
	public void unbecomeAll() {
		
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
		
	}
	
	public void send(ActorMessage<?> message, String alias) {
		
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
					String.format("%s - System: actor (%s) - Unhandled message (%s) from source (%s)",
						system.name, actorLabel(this), message.toString(), actorLabel(sourceActor)
					));
			else
				logger().warn(
					String.format("%s - System: actor (%s) - Unhandled message (%s) from unavaible source (???)",
						system.name, actorLabel(this), message.toString()
					));
		}
	}
	
	public void setAlias(String alias) {
		system.setAlias(id, alias);
	}
	
	public UUID addChild(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
		
	}
	
	public UUID addChild(ActorFactory factory) {
		
	}
	
	public SupervisorStrategy supervisorStrategy() {
		return new DefaultSupervisiorStrategy();
	}
	
	/**
	 * Initialize here, your actor code. Create new actors as child or send too other actors messages, 
	 * before the first message for this actor could be processed.
	 */
	public void preStart() {
		// empty
	}
	
	public void preRestart(Exception reason) {
		// empty
	}
	
	public void postRestart(Exception reason) {
		preStart();
	}
	
	public void postStop() {
		// empty
	}
	
	public void stop() {
		
	}
	
	public void watch(UUID dest) {
		
	}
	
	public void unwatch(UUID dest) {
		
	}
}
