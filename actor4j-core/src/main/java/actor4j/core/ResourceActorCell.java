/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import actor4j.annotations.Stateless;
import actor4j.core.actors.Actor;
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

	public void run(ActorMessage<?> message) {
		try {
			boolean successful = true;
			if (stateful) {
				try {
					// Spinlock
					while (lock.compareAndSet(false, true));
				
					successful = (status==false) ? status=true : false;
				
					if (!successful) {
						queue.offer(message);
					}
				}
				finally {
					lock.set(false);
				}
			}
			
			if (successful) {
				internal_receive(message);
				
				if (stateful) {
					boolean loop = true;
					while (loop) {
						while ((message=queue.poll())!=null)
							internal_receive(message);
						
						try {
							// Spinlock
							while (lock.compareAndSet(false, true));
						
							if (queue.peek()==null) {
								status = false;
								loop   = false;
							}	
						}
						finally {
							lock.set(false);
						}
					}
				}
			}
		}
		catch(Exception e) {
			SafetyManager.getInstance().notifyErrorHandler(e, "resource", id);
			system.actorStrategyOnFailure.handle(this, e);
		}	
	}
}
