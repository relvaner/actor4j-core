/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.actors;

import java.util.Queue;
import java.util.UUID;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.supervisor.DefaultSupervisiorStrategy;
import actor4j.core.supervisor.SupervisorStrategy;
import actor4j.core.utils.ActorFactory;
import actor4j.function.Consumer;
import actor4j.function.Predicate;

import static actor4j.core.ActorProtocolTag.*;

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
	
	public ActorCell getCell() {
		return cell;
	}

	public void setCell(ActorCell cell) {
		this.cell = cell;
	}
	
	public ActorSystem getSystem() {
		return cell.getSystem();
	}

	public String getName() {
		return name;
	}
	
	public UUID getId() {
		return cell.getId();
	}
	
	public UUID self() {
		return cell.getId();
	}
	
	public UUID getParent() {
		return cell.getParent();
	}
	
	public Queue<UUID> getChildren() {
		return cell.getChildren();
	}
	
	public boolean isRoot() {
		return cell.isRoot();
	}
	
	public boolean isRootInUser() {
		return cell.isRootInUser();
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
		cell.send(message);
	}
	
	public void send(ActorMessage<?> message, String alias) {
		cell.send(message, alias);
	}
	
	public void send(ActorMessage<?> message, UUID dest) {
		message.source = self();
		message.dest   = dest;
		send(message);
	}
	
	public void forward(ActorMessage<?> message, UUID dest) {
		message.dest   = dest;
		send(message);
	}
	
	public void unhandled(ActorMessage<?> message) {
		cell.unhandled(message);
	}
	
	public void setAlias(String alias) {
		cell.getSystem().setAlias(self(), alias);
	}
	
	public UUID addChild(Class<? extends Actor> clazz, Object... args) {
		return cell.addChild(clazz, args);
	}
	
	public UUID addChild(ActorFactory factory) {
		return cell.addChild(factory);
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
		cell.restart(reason);
	}
	
	public void postRestart(Exception reason) {
		preStart();
	}
	
	public void postStop() {
		// empty
	}
	
	public void stop() {
		cell.stop();
	}
	
	public void watch(UUID dest) {
		cell.watch(dest);
	}
	
	public void unwatch(UUID dest) {
		cell.unwatch(dest);
	}
}
