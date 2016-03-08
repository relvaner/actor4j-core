/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyzer;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;
import tools4j.utils.Timer;
import tools4j.utils.TimerListener;

public abstract class ActorAnalyzerThread extends Thread {
	protected Queue<ActorMessage<?>> outerQueueL2;
	protected Queue<ActorMessage<?>> outerQueueL1;
	
	protected ActorSystemImpl system;
	
	protected AtomicLong counter;
	
	protected Timer timer;
	
	public ActorAnalyzerThread(long delay) {
		super("actor4j-analyzer-thread");
		
		outerQueueL2 = new ConcurrentLinkedQueue<>();
		outerQueueL1 = new LinkedList<>();
		
		counter = new AtomicLong(0);
		
		timer = new Timer(delay, new TimerListener() {
			@Override
			public void task() {	
				update(system.getCells());
			}
		});
	}
	
	protected void setSystem(ActorSystemImpl system) {
		this.system = system;
	}
	
	protected boolean poll(Queue<ActorMessage<?>> queue) {
		boolean result = false;
		
		ActorMessage<?> message = queue.poll();
		if (message!=null) {
			analyze(message);
			counter.getAndIncrement();
			
			result = true;
		} 
		
		return result;
	}
	
	protected abstract void analyze(ActorMessage<?> message);
	
	protected abstract void update(Map<UUID, ActorCell> cells);
	
	@Override
	public void run() {
		timer.start();
		
		boolean hasNextOuter  = false;
		
		while (!isInterrupted()) { 
			hasNextOuter = poll(outerQueueL1);
			if (!hasNextOuter && outerQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				for (int j=0; (message=outerQueueL2.poll())!=null && j<10000; j++)
					outerQueueL1.offer(message);
				
				hasNextOuter = poll(outerQueueL1);
			}
			
			if ((!hasNextOuter))
				if (!system.isSoftMode())
					yield();
				else {
					try {
						sleep(system.getSoftSleep());
					} catch (InterruptedException e) {
						interrupt();
					}
				}
		}
		
		timer.interrupt();
	}
	
	public Queue<ActorMessage<?>> getOuterQueue() {
		return outerQueueL2;
	}
}
