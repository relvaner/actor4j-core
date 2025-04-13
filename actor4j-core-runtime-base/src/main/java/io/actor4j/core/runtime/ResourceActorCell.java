/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.messages.ActorMessage;

public class ResourceActorCell extends BaseActorCell {
	protected final boolean stateful;
	protected volatile boolean status; // volatile not necessary!
	protected final AtomicBoolean lock;
	protected final Queue<ActorMessage<?>> queue;
	protected final boolean bulk;

	public ResourceActorCell(InternalActorSystem system, Actor actor) {
		super(system, actor);
		
		if (actor instanceof ResourceActor && ((ResourceActor)actor).isStateful()) {
			stateful = true;
			lock   	 = new AtomicBoolean(false);
			queue  	 = new ConcurrentLinkedQueue<>();
			bulk     = ((ResourceActor)actor).isBulk();
		}
		else {
			stateful = false;
			lock = null;
			queue = null;
			bulk = false;
		}
	}
	
	@Override
	public void preStart() {
		super.preStart();
	}
	
	public boolean beforeRun(ActorMessage<?> message) {
		boolean result = true;
		
		if (stateful) {
			// Spinlock
			while (!lock.compareAndSet(false, true));
			try {
				result = (status==false) ? status=true : false;
			
				if (!result) {
					queue.offer(message);
				}
			}
			finally {
				lock.set(false);
			}
		}
		
		return result;
	}

	public void run(ActorMessage<?> message) {
		try {
			before();
			
			if (!bulk)
				internal_receive(message);
			
			if (stateful) {
				while (true) {
					if (!bulk) {
						while ((message=queue.poll())!=null)
							internal_receive(message);
					}
					else {
						List<ActorMessage<?>> bulkList = new LinkedList<>();
						bulkList.add(message);
						while ((message=queue.poll())!=null)
							bulkList.add(message);
						internal_receive(ActorMessage.create(new ImmutableList<>(bulkList), 0, system.SYSTEM_ID(), id));
					}
					
					// Spinlock
					while (!lock.compareAndSet(false, true));
					try {
						if (queue.peek()==null) {
							status = false;
							break;
						}	
					}
					finally {
						lock.set(false);
					}
				}
			}
			
			after();
		}
		catch(Exception e) {
			system.getExecutorService().getFaultToleranceManager().notifyErrorHandler(e, ActorSystemError.RESOURCE_ACTOR, id);
			system.getStrategyOnFailure().handle(this, e);
		}	
	}
	
	public void before() {
		((ResourceActor)actor).before();
	}
	
	public void after() {
		((ResourceActor)actor).after();
	}
	
	@Deprecated
	@Override
	public void unsafe_send(ActorMessage<?> message) {
		// empty
	}
	
	@Deprecated
	@Override
	public void unsafe_send(ActorMessage<?> message, String alias) {
		// empty
	}
}
