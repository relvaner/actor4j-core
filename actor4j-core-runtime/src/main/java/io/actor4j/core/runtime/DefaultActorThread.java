/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.runtime;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import io.actor4j.core.messages.ActorMessage;

public abstract class DefaultActorThread extends ActorThread {
	protected /*quasi final*/ Queue<ActorMessage<?>> directiveQueue;
	protected /*quasi final*/ Queue<ActorMessage<?>> priorityQueue;
	protected /*quasi final*/ Queue<ActorMessage<?>> innerQueue;
	protected /*quasi final*/ Queue<ActorMessage<?>> outerQueueL2;
	protected /*quasi final*/ Queue<ActorMessage<?>> outerQueueL1;
	protected /*quasi final*/ Queue<ActorMessage<?>> serverQueueL2;
	protected /*quasi final*/ Queue<ActorMessage<?>> serverQueueL1;
	
	protected final Object blocker = new Object();
	protected final AtomicBoolean parked;
	
	final ActorThreadMode threadMode;
	final boolean serverMode;
	
	final int maxThroughput;
	final int queueSize;
	final int bufferQueueSize;
	
	final int maxSpins;
	final int highLoad;
	
	final long maxSleepTime;
	
	public DefaultActorThread(ThreadGroup group, String name, InternalActorSystem system) {
		super(group, name, system);
		
		threadMode		 = system.getConfig().threadMode(); 
		serverMode       = system.getConfig().serverMode(); 
		
		maxThroughput    = system.getConfig().throughput();
		queueSize        = system.getConfig().queueSize();
		bufferQueueSize  = system.getConfig().bufferQueueSize();
		
		maxSpins         = system.getConfig().maxSpins();
		highLoad         = system.getConfig().highLoad();
		maxSleepTime     = system.getConfig().sleepTime();
		
		parked = new AtomicBoolean(false);
		
		configQueues();
	}
	
	
	public abstract void configQueues();
	
	@Override
	public void directiveQueue(ActorMessage<?> message) {
		directiveQueue.offer(message);
	}
	
	@Override
	public void priorityQueue(ActorMessage<?> message) {
		priorityQueue.offer(message);
	}
	
	@Override
	public void serverQueue(ActorMessage<?> message) {
		serverQueueL2.offer(message);
	}
	
	@Override
	public void outerQueue(ActorMessage<?> message) {
		outerQueueL2.offer(message);
	}
	
	@Override
	public void innerQueue(ActorMessage<?> message) {
		innerQueue.offer(message);
	}
	
	@Override
	public void onRun() {
		boolean hasNextDirective;
		boolean hasNextPriority;
		int hasNextServer;
		int hasNextOuter;
		int hasNextInner;
		int spins = 0;
		int loads = 0;
		
		while (!isInterrupted()) {
			hasNextDirective = false;
			hasNextPriority  = false;
			hasNextServer    = 0;
			hasNextOuter     = 0;
			hasNextInner     = 0;
			
			while (poll(directiveQueue)) 
				hasNextDirective=true;
			
			while (poll(priorityQueue)) 
				hasNextPriority=true;
			
			if (serverMode) {
				for (; hasNextServer<maxThroughput && poll(serverQueueL1); hasNextServer++);
				if (hasNextServer<maxThroughput && serverQueueL2.peek()!=null) {
					ActorMessage<?> message = null;
					int delta = bufferQueueSize-serverQueueL1.size();
					for (int j=0; j<delta && (message=serverQueueL2.poll())!=null; j++)
						serverQueueL1.offer(message);
				
					for (; hasNextServer<maxThroughput && poll(serverQueueL1); hasNextServer++);
				}
			}
			
			for (; hasNextOuter<maxThroughput && poll(outerQueueL1); hasNextOuter++);
			if (hasNextOuter<maxThroughput && outerQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				int delta = bufferQueueSize-outerQueueL1.size();
				for (int j=0; j<delta && (message=outerQueueL2.poll())!=null; j++)
					outerQueueL1.offer(message);

				for (; hasNextOuter<maxThroughput && poll(outerQueueL1); hasNextOuter++);
			}
			
			for (; hasNextInner<maxThroughput && poll(innerQueue); hasNextInner++);
			
			if (hasNextInner==0 && hasNextOuter==0 && hasNextServer==0 && !hasNextPriority && !hasNextDirective) {
				if (spins>highLoad) {
					loads = 0;
					threadLoad.set(false);
				}
				spins++;
				if (spins>maxSpins) {
					spins = 0;
					if (threadMode==ActorThreadMode.PARK) {
						parked.set(true);
						if (   !outerQueueL2.isEmpty() 
							|| !directiveQueue.isEmpty() 
							|| (serverMode && !serverQueueL2.isEmpty()) 
							|| !priorityQueue.isEmpty()) {
							parked.set(false);
							continue;
						}
						LockSupport.park(blocker);
						parked.set(false);
						if (isInterrupted())
							interrupt();
					}
					else if (threadMode==ActorThreadMode.SLEEP) {
						try {
							sleep(maxSleepTime);
						} catch (InterruptedException e) {
							interrupt();
						}
					}
					else
						Thread.yield();
				}
				else
					Thread.onSpinWait();
			}
			else {
				spins = 0;
				if (loads>highLoad)
					threadLoad.set(true);
				else
					loads++;
			}
		}		
	}
	
	@Override
	protected void newMessage() {
//		if (threadMode==ActorThreadMode.PARK && parked.compareAndSet(true, false)) // prevents multiple calls of unpark
		if (threadMode==ActorThreadMode.PARK && parked.get())
			LockSupport.unpark(this);			
	}
	
	@Override
	public Queue<ActorMessage<?>> getDirectiveQueue() {
		return directiveQueue;
	}
	
	@Override
	public Queue<ActorMessage<?>> getPriorityQueue() {
		return priorityQueue;
	}
	
	@Override
	public Queue<ActorMessage<?>> getServerQueue() {
		return serverQueueL2;
	}
	
	@Override
	public Queue<ActorMessage<?>> getOuterQueue() {
		return outerQueueL2;
	}

	@Override
	public Queue<ActorMessage<?>> getInnerQueue() {
		return innerQueue;
	}
}
