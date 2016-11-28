/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import actor4j.core.messages.ActorMessage;
import safety4j.Method;
import safety4j.SafetyMethod;

public abstract class ActorThread extends Thread {
	protected final UUID uuid; // for safety
	
	protected ActorSystemImpl system;
	
	protected AtomicLong counter;
	protected Runnable onTermination;
	
	protected static int index;
	
	public ActorThread(ActorSystemImpl system) {
		super("actor4j-worker-thread-"+index);
		index++;
		
		this.system = system;
		uuid = UUID.randomUUID();
		
		counter = new AtomicLong(0);
	}
	
	protected void safetyMethod(ActorMessage<?> message, ActorCell cell) {
		try {
			cell.internal_receive(message);
		}
		catch(Exception e) {
			system.executerService.safetyManager.notifyErrorHandler(e, "actor", cell.id);
			system.actorStrategyOnFailure.handle(cell, e);
		}	
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			ActorCell cell = system.cells.get(message.dest);
			if (cell!=null)
				safetyMethod(message, cell);
			if (system.counterEnabled)
				counter.getAndIncrement();
			
			result = true;
		} 
		
		return result;
	}
	
	public abstract void onRun();
		
	@Override
	public void run() {
		SafetyMethod.run(system.executerService.safetyManager, new Method() {
			@Override
			public void run(UUID uuid) {
				onRun();
				
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
	
	public long getCount() {
		return counter.longValue();
	}
	
	public UUID getUUID() {
		return uuid;
	}
}
