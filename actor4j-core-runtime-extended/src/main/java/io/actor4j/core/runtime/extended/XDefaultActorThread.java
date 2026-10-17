/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.extended;

import java.util.concurrent.locks.LockSupport;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.ActorThreadMode;
import io.actor4j.core.runtime.DefaultActorThread;
import io.actor4j.core.runtime.InternalActorSystem;

// @See DefaultActorThread
public abstract class XDefaultActorThread extends DefaultActorThread {
	public XDefaultActorThread(ThreadGroup group, String name, InternalActorSystem system) {
		super(group, name, system);
	}
	
	public static /*value*/ class MessageCounters {
		public int hasNextServer;
		public int hasNextOuter;
		public int hasNextInner;
	}
	
	public void processMessages(MessageCounters ctn) {
		if (serverMode) {
			for (; ctn.hasNextServer<maxThroughput && poll(serverQueueL1); ctn.hasNextServer++);
			if (ctn.hasNextServer<maxThroughput && serverQueueL2.peek()!=null) {
				ActorMessage<?> message = null;
				int delta = bufferQueueSize-serverQueueL1.size();
				for (int j=0; j<delta && (message=serverQueueL2.poll())!=null; j++)
					serverQueueL1.offer(message);
			
				for (; ctn.hasNextServer<maxThroughput && poll(serverQueueL1); ctn.hasNextServer++);
			}
		}
		
		for (; ctn.hasNextOuter<maxThroughput && poll(outerQueueL1); ctn.hasNextOuter++);
		if (ctn.hasNextOuter<maxThroughput && outerQueueL2.peek()!=null) {
			ActorMessage<?> message = null;
			int delta = bufferQueueSize-outerQueueL1.size();
			for (int j=0; j<delta && (message=outerQueueL2.poll())!=null; j++)
				outerQueueL1.offer(message);

			for (; ctn.hasNextOuter<maxThroughput && poll(outerQueueL1); ctn.hasNextOuter++);
		}
		
		for (; ctn.hasNextInner<maxThroughput && poll(innerQueue); ctn.hasNextInner++);
	}
	
	@Override
	public void onRun() {
		boolean hasNextDirective;
		boolean hasNextPriority;
		MessageCounters ctn = new MessageCounters();
		int spins = 0;
		int processed;
		long startTime;
		long iterationStart;
		
		while (!isInterrupted()) {
			hasNextDirective = false;
			hasNextPriority  = false;
			ctn.hasNextServer = 0;
			ctn.hasNextOuter  = 0;
			ctn.hasNextInner  = 0;
			processed        = 0;
			
			startTime = startLoopMetrics();
			iterationStart = busySince==0 ? System.nanoTime() : 0;
			
			while (poll(directiveQueue)) 
				hasNextDirective=true;
			
			while (poll(priorityQueue)) 
				hasNextPriority=true;
			
			processMessages(ctn);
			
			processed = ctn.hasNextInner + ctn.hasNextOuter + ctn.hasNextServer;
			if (processed==0 && !hasNextPriority && !hasNextDirective) {
				if (busySince!=0) {
					busyNanos += System.nanoTime()-busySince;
					busySince = 0;
				}
				spins++;
				if (spins>maxSpins) {
					spins = 0;
					if (threadMode==ActorThreadMode.PARK) {
						parked.set(true);
						if (hasPendingMessages()) {
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
				stopLoopMetrics(startTime, processed);
				if (busySince==0)
					busySince = iterationStart;
			}
		}		
	}
	
	protected boolean hasPendingMessages() {
		return !outerQueueL2.isEmpty() || !directiveQueue.isEmpty() || (serverMode && !serverQueueL2.isEmpty()) || !priorityQueue.isEmpty();
	}
}
