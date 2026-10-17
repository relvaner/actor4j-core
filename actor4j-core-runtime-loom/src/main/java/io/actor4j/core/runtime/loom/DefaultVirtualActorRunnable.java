/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.loom;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public class DefaultVirtualActorRunnable extends VirtualActorRunnable {
	protected final Queue<ActorMessage<?>> directiveQueue;
	protected final Queue<ActorMessage<?>> outerQueue;
	
//	protected final AtomicBoolean parked;
	
	protected int hasNextDirective;
	protected int hasNextOuter;
	protected int totalProcessed;
	
	public DefaultVirtualActorRunnable(InternalActorSystem system, InternalActorCell cell, Runnable onTermination) {
		super(system, cell, onTermination);

		directiveQueue = new ConcurrentLinkedQueue<>();
		outerQueue = new ConcurrentLinkedQueue<>();
		
//		parked = new AtomicBoolean(false);
	}

	@Override
	public int processMessagesForMetrics() {
		hasNextDirective = 0;
		hasNextOuter = 0;
		totalProcessed = 0;
		
		ActorMessage<?> msg = null;
		for (; (msg=directiveQueue().poll())!=null; hasNextDirective++)
			faultToleranceMethod(msg);
		
		for (; hasNextOuter<system.getConfig().throughput() && (msg=outerQueue().poll())!=null; hasNextOuter++)
			faultToleranceMethod(msg);
		
//		for (; (msg=directiveQueue().poll())!=null; hasNextDirective++)
//			onPoll(msg);
		
		totalProcessed = hasNextDirective + hasNextOuter;
		if (totalProcessed==0) {
			if (outerQueue().isEmpty() && directiveQueue().isEmpty()) {
				LockSupport.park(Thread.currentThread());
				if (Thread.interrupted())
					Thread.currentThread().interrupt();
			}
////		parked.set(true);
//			if (!outerQueue().isEmpty() || !directiveQueue().isEmpty()) {
////			parked.set(false);
//				return 0;
//			}
//			LockSupport.park(Thread.currentThread());
////		parked.set(false);
//			if (Thread.interrupted())
//				   Thread.currentThread().interrupt();
		}
		else {
			if (system.getConfig().counterEnabled().get())
				getMetrics().counter.add(totalProcessed);
		}
		
		return totalProcessed;
	}
	
	@Override
	public void onRun() {
		while ( !(Thread.interrupted() || terminated.get()) ) {
			if (system.getConfig().metricsEnabled().get())
				metrics();
			else
				processMessages();
		}
	}
	
	@Override
	public void newMessage(Thread t) {
//		if (parked.compareAndSet(true, false))
			LockSupport.unpark(t);
	}
	
	@Override
	public Queue<ActorMessage<?>> directiveQueue() {
		return directiveQueue;
	}

	@Override
	public Queue<ActorMessage<?>> outerQueue() {
		return outerQueue;
	}

	@Deprecated
	@Override
	public long getCount() {
		// Not used!
		return 0;
	}

	@Deprecated
	@Override
	public AtomicBoolean getLoad() {
		// Not used!
		return null;
	}

	@Deprecated
	@Override
	public Queue<Long> getProcessingTimeSamples() {
		// Not used!
		return null;
	}
	
	@Deprecated
	@Override
	public AtomicInteger getProcessingTimeSampleCount() {
		// Not used!
		return null;
	}
	
	@Deprecated
	@Override
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		// Not used!
		return null;
	}
}
