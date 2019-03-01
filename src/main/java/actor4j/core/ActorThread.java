/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package actor4j.core;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import actor4j.core.messages.ActorMessage;
import actor4j.core.safety.Method;
import actor4j.core.safety.SafetyMethod;

public abstract class ActorThread extends Thread {
	protected final UUID uuid; // for safety
	
	protected final ActorSystemImpl system;
	
	protected final AtomicLong counter;
	protected Runnable onTermination;
	
	public ActorThread(ThreadGroup group, String name, ActorSystemImpl system) {
		super(group, name);
		
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
		SafetyMethod.runAndCatchThrowable(system.executerService.safetyManager, new Method() {
			@Override
			public void run(UUID uuid) {
				onRun();
				
				if (onTermination!=null)
					onTermination.run();
			}
			
			@Override
			public void error(Throwable t) {
				t.printStackTrace();
			}
			
			@Override
			public void after() {
			}
		}, uuid);
	}
	
	public AtomicLong getCounter() {
		return counter;
	}

	public long getCount() {
		return counter.longValue();
	}
	
	public abstract Queue<ActorMessage<?>> getInnerQueue();
	
	public abstract Queue<ActorMessage<?>> getOuterQueue();
	
	public UUID getUUID() {
		return uuid;
	}
}
