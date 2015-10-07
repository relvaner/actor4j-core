/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.jctools.queues.MpscArrayQueue;

import safety4j.Method;
import safety4j.SafetyManager;
import safety4j.SafetyMethod;

public class ActorThread extends Thread {
	protected final UUID uuid; // for safety
	
	protected Queue<ActorMessage<?>> innerQueue;
	protected Queue<ActorMessage<?>> outerQueueL2;
	protected Queue<ActorMessage<?>> outerQueueL1;
	protected Queue<ActorMessage<?>> serverQueueL2;
	protected Queue<ActorMessage<?>> serverQueueL1;
	
	protected ActorSystem system;
	
	protected AtomicLong counter;
	protected Runnable onTermination;
	
	protected static int index;
	
	public ActorThread(ActorSystem system) {
		super("actor4j-worker-thread-"+index);
		index++;
		
		this.system = system;
		uuid = UUID.randomUUID();
		
		innerQueue    = new CircularFifoQueue<>(50000); //new LinkedList<>();
		outerQueueL2  = new MpscArrayQueue<>(50000);    //new ConcurrentLinkedQueue<>();
		outerQueueL1  = new CircularFifoQueue<>(10000);
		serverQueueL2 = new MpscArrayQueue<>(50000);    //new ConcurrentLinkedQueue<>();
		serverQueueL1 = new CircularFifoQueue<>(10000); //new LinkedList<>();
		
		counter = new AtomicLong(0);
	}
	
	protected void safetyMethod(ActorMessage<?> message, Actor actor) {
		try {
			actor.internal_receive(message);
		}
		catch(Exception e) {
			SafetyManager.getInstance().notifyErrorHandler(e, "actor", actor.getId());
			system.actorStrategyOnFailure.handle(actor, e);
		}	
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			Actor actor = system.actors.get(message.dest);
			safetyMethod(message, actor);
			counter.getAndIncrement();
			
			result = true;
		} 
		
		return result;
	}
		
	@Override
	public void run() {
		SafetyMethod.run(new Method() {
			@Override
			public void run(UUID uuid) {
				boolean hasNextServer = false;
				boolean hasNextOuter  = false;
				boolean hasNextInner  = false;
				
				while (!isInterrupted()) { 
					if (system.serverMode) {
						hasNextServer = poll(serverQueueL1);
						if (!hasNextServer && serverQueueL2.peek()!=null) {
							ActorMessage<?> message = null;
							for (int j=0; (message=serverQueueL2.poll())!=null && j<10000; j++)
								serverQueueL1.offer(message);
						
							hasNextServer = poll(serverQueueL1);
						}
					}
					
					hasNextOuter = poll(outerQueueL1);
					if (!hasNextOuter && outerQueueL2.peek()!=null) {
						ActorMessage<?> message = null;
						for (int j=0; (message=outerQueueL2.poll())!=null && j<10000; j++)
							outerQueueL1.offer(message);
						
						hasNextOuter = poll(outerQueueL1);
					}
					
					hasNextInner = poll(innerQueue);
					if ((!hasNextOuter && !hasNextInner && !hasNextServer))
						if (!system.softMode)
							yield();
						else {
							try {
								sleep(system.softSleep);
							} catch (InterruptedException e) {
								interrupt();
							}
						}
				}
				
				if (onTermination!=null)
					onTermination.run();
			}
			
			@Override
			public void error(Exception e) {
			}
			
			@Override
			public void after() {
			}
		}, uuid);
	}
	
	public Queue<ActorMessage<?>> getInnerQueue() {
		return innerQueue;
	}
	
	public Queue<ActorMessage<?>> getOuterQueue() {
		return outerQueueL2;
	}
	
	public long getCount() {
		return counter.longValue();
	}
	
	public UUID getUUID() {
		return uuid;
	}
}
