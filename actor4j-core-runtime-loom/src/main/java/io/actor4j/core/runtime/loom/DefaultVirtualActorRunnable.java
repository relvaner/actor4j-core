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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public class DefaultVirtualActorRunnable extends VirtualActorRunnable {
	protected final Queue<ActorMessage<?>> directiveQueue;
	protected final Queue<ActorMessage<?>> outerQueue;
	
	public DefaultVirtualActorRunnable(InternalActorSystem system, InternalActorCell cell, BiConsumer<ActorMessage<?>, InternalActorCell> failsafeMethod, Runnable onTermination, AtomicLong counter) {
		super(system, cell, failsafeMethod, onTermination, counter);

		directiveQueue = new ConcurrentLinkedQueue<>();
		outerQueue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public void onRun() {
		int hasNextDirective;
		int hasNextOuter;
		
		while ( !(Thread.interrupted() || terminated.get()) ) {
			hasNextDirective = 0;
			hasNextOuter = 0;
			
			ActorMessage<?> msg = null;
			for (; (msg=directiveQueue().poll())!=null; hasNextDirective++)
				faultToleranceMethod.accept(msg, cell);
			
			for (; hasNextOuter<system.getConfig().throughput() && (msg=outerQueue().poll())!=null; hasNextOuter++)
				faultToleranceMethod.accept(msg, cell);
			
			for (; (msg=directiveQueue().poll())!=null; hasNextDirective++)
				faultToleranceMethod.accept(msg, cell);
			
			if (hasNextOuter==0 && hasNextDirective==0)
				LockSupport.park(Thread.currentThread());
			else {
				if (system.getConfig().trackRequestRatePerActor().get())
					cell.getRequestRate().addAndGet(hasNextDirective+hasNextOuter);
				if (system.getConfig().counterEnabled().get())
					counter.addAndGet(hasNextDirective+hasNextOuter);
			}
		}
	}
	
	@Override
	public void newMessage(Thread t) {
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

	@Override
	public long getCount() {
		// Not used!
		return 0;
	}

	@Override
	public AtomicBoolean getLoad() {
		// Not used!
		return null;
	}

	@Override
	public Queue<Long> getProcessingTimeSamples() {
		// Not used!
		return null;
	}
	
	@Override
	public AtomicInteger getProcessingTimeSampleCount() {
		// Not used!
		return null;
	}
	
	@Override
	public AtomicInteger getCellsProcessingTimeSampleCount() {
		// Not used!
		return null;
	}
}
