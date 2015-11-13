/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.jctools.queues.MpscArrayQueue;

import actor4j.core.messages.ActorMessage;

public class DefaultActorThread extends ActorThread {
	protected Queue<ActorMessage<?>> directiveQueue;
	protected Queue<ActorMessage<?>> innerQueue;
	protected Queue<ActorMessage<?>> outerQueueL2;
	protected Queue<ActorMessage<?>> outerQueueL1;
	protected Queue<ActorMessage<?>> serverQueueL2;
	protected Queue<ActorMessage<?>> serverQueueL1;
	
	public DefaultActorThread(ActorSystemImpl system) {
		super(system);
		
		directiveQueue = new MpscArrayQueue<>(system.getQueueSize());
		serverQueueL2  = new MpscArrayQueue<>(system.getQueueSize());
		serverQueueL1  = new LinkedList<>();
		outerQueueL2   = new MpscArrayQueue<>(system.getQueueSize());
		outerQueueL1   = new LinkedList<>();
		innerQueue     = new CircularFifoQueue<>(system.getQueueSize());
	}
	
	@Override
	public void onRun() {
		boolean hasNextDirective;
		long hasNextServer;
		long hasNextOuter;
		long hasNextInner;
		
		while (!isInterrupted()) {
			hasNextDirective = false;
			hasNextServer    = 0;
			hasNextOuter     = 0;
			hasNextInner     = 0;
			
			while (poll(directiveQueue)) 
				hasNextDirective=true;
			
			if (system.isClientMode()) {
				if (!poll(serverQueueL1) && serverQueueL2.peek()!=null) {
					ActorMessage<?> message = null;
					for (int j=0; (message=serverQueueL2.poll())!=null && j<system.getBufferQueueSize(); j++)
						serverQueueL1.offer(message);
				
					for (; poll(serverQueueL1) && hasNextServer<system.throughput; hasNextServer++);
				}
				else
					hasNextServer++;
			}
			
			/*
			for (; poll(outerQueueL1) && hasNextOuter<system.throughput; hasNextOuter++);
			if (hasNextOuter==0 && outerQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				for (int j=0; (message=outerQueueL2.poll())!=null && j<system.getBufferQueueSize(); j++)
					outerQueueL1.offer(message);
				
				for (; poll(outerQueueL1) && hasNextOuter<system.throughput; hasNextOuter++);
			}
			*/
			if (!poll(outerQueueL1) && outerQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				for (int j=0; (message=outerQueueL2.poll())!=null && j<system.getBufferQueueSize(); j++)
					outerQueueL1.offer(message);
				
				for (; poll(outerQueueL1) && hasNextOuter<system.throughput; hasNextOuter++);
			}
			else
				hasNextOuter++;
			
			for (; poll(innerQueue) && hasNextInner<system.throughput; hasNextInner++);
			
			if ((hasNextInner==0 && hasNextOuter==0 && hasNextServer==0 && !hasNextDirective)) {
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
		}		
	}
	
	public Queue<ActorMessage<?>> getInnerQueue() {
		return innerQueue;
	}
	
	public Queue<ActorMessage<?>> getOuterQueue() {
		return outerQueueL2;
	}
}
