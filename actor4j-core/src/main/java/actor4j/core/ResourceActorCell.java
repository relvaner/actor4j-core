/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import actor4j.annotations.Stateless;
import actor4j.core.actors.Actor;
import actor4j.core.actors.ResourceActor;
import actor4j.core.messages.ActorMessage;
import safety4j.SafetyManager;

public class ResourceActorCell extends ActorCell {
	protected boolean stateful;
	protected volatile boolean status; // volatile not necessary!
	protected AtomicBoolean lock;
	protected Queue<ActorMessage<?>> queue;

	public ResourceActorCell(ActorSystemImpl system, Actor actor) {
		super(system, actor);
	}
	
	@Override
	public void preStart() {
		if (!actor.getClass().isAnnotationPresent(Stateless.class)) {
			stateful = true;
			lock   	 = new AtomicBoolean(false);
			queue  	 = new ConcurrentLinkedQueue<>();
		}
		super.preStart();
	}
	
	public boolean beforeRun(ActorMessage<?> message) {
		boolean result = true;
		
		if (stateful) {
			try {
				// Spinlock
				while (lock.compareAndSet(false, true));
			
				result = (status==false) ? status=true : false;
			
				if (!result) {
					queue.offer(message);
				}
			}
			finally {
				lock.set(false);
			}
		}
		
		return result;
	}

	public void run(ActorMessage<?> message) {
		try {
			before();
			
			internal_receive(message);
			
			if (stateful) {
				while (true) {
					while ((message=queue.poll())!=null)
						internal_receive(message);
					
					try {
						// Spinlock
						while (lock.compareAndSet(false, true));
					
						if (queue.peek()==null) {
							status = false;
							break;
						}	
					}
					finally {
						lock.set(false);
					}
				}
			}
			
			after();
		}
		catch(Exception e) {
			SafetyManager.getInstance().notifyErrorHandler(e, "resource", id);
			system.actorStrategyOnFailure.handle(this, e);
		}	
	}
	
	public void before() {
		((ResourceActor)actor).before();
	}
	
	public void after() {
		((ResourceActor)actor).after();
	}
}
