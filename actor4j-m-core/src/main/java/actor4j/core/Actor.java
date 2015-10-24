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
	public ActorSystem system;
	
	public UUID id;
	public String name;
	
	public UUID parent;
	public Queue<UUID> children;
	
	public Deque<Consumer<ActorMessage<?>>> behaviourStack;
	
	public Queue<ActorMessage<?>> stash; //must be initialized by hand
	
	public RestartProtocol restartProtocol;
	public StopProtocol stopProtocol;
	
	public Queue<UUID> deathWatcher;
	
	public static final int POISONPILL = INTERNAL_STOP;
	public static final int TERMINATED = INTERNAL_STOP_SUCCESS;
	public static final int KILL       = INTERNAL_KILL;
	
	public static final int STOP       = INTERNAL_STOP;
	public static final int RESTART    = INTERNAL_RESTART;
	
	public Function<ActorMessage<?>, Boolean> processedDirective;
	public boolean activeDirectiveBehaviour;
			
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
		
		this.id   = UUID.randomUUID();
		this.name = name;
		
		children = new ConcurrentLinkedQueue<>();
		
		behaviourStack = new ArrayDeque<>();
		
		restartProtocol = new RestartProtocol(this);
		stopProtocol = new StopProtocol(this);
		
		deathWatcher =  new ConcurrentLinkedQueue<>();
		
		processedDirective = new Function<ActorMessage<?>, Boolean>() {
			@Override
			public Boolean apply(ActorMessage<?> message) {
				boolean result = false;
				
				if (isDirective(message) && !activeDirectiveBehaviour) {
					result = true;
					if (message.tag==INTERNAL_RESTART || message.tag==INTERNAL_STOP)
						activeDirectiveBehaviour = true;
						
					if (message.tag==INTERNAL_RESTART) {
						if (message.value instanceof Exception)
							preRestart((Exception)message.value);
						else
							preRestart(null);
					}
					else if (message.tag==INTERNAL_STOP)
						stop();
					else if (message.tag==INTERNAL_KILL) 
						throw new ActorKilledException();
					else
						result = false;
				}
				
				return result;
			}	
		};
	}
	
	public UUID self() {
		return id;
	}
	
	public boolean isRoot() {
		return (parent==null);
	}
	
	public boolean isRootInUser() {
		return (parent==system.USER_ID);
	}
	
	/**
	 * Don't use this method within your actor code. It's an internal method.
	 */
	public void internal_receive(ActorMessage<?> message) {
		if (!processedDirective.apply(message)) {
			Consumer<ActorMessage<?>> behaviour = behaviourStack.peek();
			if (behaviour==null)
				receive(message);
			else
				behaviour.accept(message);	
		}
	}
	
	public abstract void receive(ActorMessage<?> message);
	
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
		system.messageDispatcher.post(message, self());
	}
	
	public void send(ActorMessage<?> message, String alias) {
		system.messageDispatcher.post(message, self(), alias);
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
	
	/**
	 * Don't use this method within your actor code. It's an internal method.
	 */
	public UUID internal_addChild(Actor actor) {
		actor.parent = id;
		children.add(actor.id);
		system.internal_addActor(actor);
		system.messageDispatcher.registerActor(actor);
		/* preStart */
		actor.preStart();
		
		return actor.id;
	}
	
	public UUID addChild(Class<? extends Actor> clazz, Object... args) throws ActorInitializationException {
		InjectorParam[] params = new InjectorParam[args.length];
		for (int i=0; i<args.length; i++)
			params[i] = InjectorParam.createWithObj(args[i]);
		
		UUID temp = UUID.randomUUID();
		system.container.registerConstructorInjector(temp, clazz, params);
		
		Actor actor;
		try {
			actor = (Actor)system.container.getInstance(temp);
			system.container.registerConstructorInjector(actor.id, clazz, params);
			system.container.unregister(temp);
		} catch (Exception e) {
			throw new ActorInitializationException();
		}
		
		return internal_addChild(actor);
	}
	
	public UUID addChild(ActorFactory factory) {
		Actor actor = factory.create();
		system.container.registerFactoryInjector(actor.id, factory);
		
		return internal_addChild(actor);
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
		restartProtocol.apply(reason);
	}
	
	public void postRestart(Exception reason) {
		preStart();
	}
	
	public void postStop() {
		// empty
	}
	
	public void stop() {
		stopProtocol.apply();
	}
	
	/**
	 * Don't use this method within your actor code. It's an internal method.
	 */
	public void internal_stop() {
		if (parent!=null)
			system.actors.get(parent).children.remove(self());
		system.messageDispatcher.unregisterActor(this);
		system.removeActor(id);
		
		Iterator<UUID> iterator = deathWatcher.iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			system.sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP_SUCCESS, self(), dest));
		}
	}
	
	public void watch(UUID dest) {
		Actor actor = system.actors.get(dest);
		if (actor!=null)
			actor.deathWatcher.add(self());
	}
	
	public void unwatch(UUID dest) {
		Actor actor = system.actors.get(dest);
		if (actor!=null)
			actor.deathWatcher.remove(self());
	}
}
