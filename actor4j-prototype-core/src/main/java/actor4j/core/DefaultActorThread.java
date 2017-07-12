/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core;

import java.util.ArrayDeque;
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
		serverQueueL1  = new ArrayDeque<>(system.getBufferQueueSize());
		outerQueueL2   = new MpscArrayQueue<>(system.getQueueSize());
		outerQueueL1   = new ArrayDeque<>(system.getBufferQueueSize());
		innerQueue     = new CircularFifoQueue<>(system.getQueueSize());
	}
	
	@Override
	public void onRun() {
		boolean hasNextDirective;
		int hasNextServer;
		int hasNextOuter;
		int hasNextInner;
		int idle = 0;
		
		while (!isInterrupted()) {
			hasNextDirective = false;
			hasNextServer    = 0;
			hasNextOuter     = 0;
			hasNextInner     = 0;
			
			while (poll(directiveQueue)) 
				hasNextDirective=true;
			
			if (system.clientMode) {
				for (; poll(serverQueueL1) && hasNextServer<system.throughput; hasNextServer++);
				if (hasNextServer<system.throughput && serverQueueL2.peek()!=null) {
					ActorMessage<?> message = null;
					for (int j=0; (message=serverQueueL2.poll())!=null && j<system.getBufferQueueSize(); j++)
						serverQueueL1.offer(message);
				
					for (; poll(serverQueueL1) && hasNextServer<system.throughput; hasNextServer++);
				}
			}
			
			for (; poll(outerQueueL1) && hasNextOuter<system.throughput; hasNextOuter++);
			if (hasNextOuter<system.throughput && outerQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				for (int j=0; (message=outerQueueL2.poll())!=null && j<system.getBufferQueueSize(); j++)
					outerQueueL1.offer(message);

				for (; poll(outerQueueL1) && hasNextOuter<system.throughput; hasNextOuter++);
			}
			
			for (; poll(innerQueue) && hasNextInner<system.throughput; hasNextInner++);
			
			if ((hasNextInner==0 && hasNextOuter==0 && hasNextServer==0 && !hasNextDirective)) {
				idle++;
				if (idle>system.idle) {
					idle = 0;
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
			}
			else
				idle = 0;
		}		
	}
	
	public Queue<ActorMessage<?>> getInnerQueue() {
		return innerQueue;
	}
	
	public Queue<ActorMessage<?>> getOuterQueue() {
		return outerQueueL2;
	}
}
