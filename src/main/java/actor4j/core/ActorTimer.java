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
package actor4j.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class ActorTimer {
	protected ActorSystemImpl system;
	protected UUID id;
	
	protected Timer timer;
	
	protected static final AtomicInteger index;
	
	static {
		index = new AtomicInteger(0);
	}
	
	public ActorTimer(ActorSystemImpl system) {
		super();
		
		this.system = system;
		timer = new Timer("actor4j-timer-thread-"+index.getAndIncrement(), false);
		
		id = UUID.randomUUID();
	}
	
	public UUID getId() {
		return id;
	}
	
	public ActorTimer scheduleOnce(final Supplier<ActorMessage<?>> supplier, final UUID dest, long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				message.dest = dest;
				system.send(message);
			}
		}, delay); 
		
		return this;
	}
	
	public ActorTimer scheduleOnce(final ActorMessage<?> message, final UUID dest, long delay) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, dest, delay);
	}
	
	public ActorTimer scheduleOnce(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				for (UUID id : group) {
					message.dest = id;
					system.send(message);
				}
			}
		}, delay); 
		
		return this;
	}
	
	public ActorTimer scheduleOnce(final ActorMessage<?> message, final ActorGroup group, long delay) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, group, delay);
	}
	
	public ActorTimer schedule(final Supplier<ActorMessage<?>> supplier, final UUID dest, long delay, long period) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				message.dest = dest;
				system.send(message);
			}
		}, delay, period); 
		
		return this;
	}
	
	public ActorTimer schedule(final ActorMessage<?> message, final UUID dest, long delay, long period) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, dest, delay, period);
	}
	
	public ActorTimer schedule(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long delay, long period) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				for (UUID id : group) {
					message.dest = id;
					system.send(message);
				}
			}
		}, delay, period); 
		
		return this;
	}
	
	public ActorTimer schedule(final ActorMessage<?> message, final ActorGroup group, long delay, long period) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, group, delay, period);
	}
	
	public void cancel() {
		timer.cancel();
		system.getExecuterService().actorTimers.remove(this);
	}
}
