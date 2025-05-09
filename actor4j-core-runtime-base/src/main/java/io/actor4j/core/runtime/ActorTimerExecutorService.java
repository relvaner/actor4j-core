/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorTimer;

public class ActorTimerExecutorService implements ActorTimer {
	protected final InternalActorSystem system;
	
	protected final ScheduledExecutorService timerExecutorService;
	
	protected static class CanceledScheduledFuture<T> implements ScheduledFuture<T> {
		public CanceledScheduledFuture() {
			super();
		}
		
		public static CanceledScheduledFuture<?> create() {
			return new CanceledScheduledFuture<>();
		}
		
		@Override
		public long getDelay(TimeUnit unit) {
			return 0;
		}

		@Override
		public int compareTo(Delayed o) {
			return 0;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return true;
		}

		@Override
		public boolean isCancelled() {
			return true;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			return null;
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return null;
		}
	}
	
	public ActorTimerExecutorService(InternalActorSystem system, int corePoolSize, String threadName) {
		super();
		
		this.system = system;
		this.timerExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, new DefaultThreadFactory(threadName));
	}
	
	public ActorTimerExecutorService(InternalActorSystem system, int corePoolSize) {
		this(system, corePoolSize, "actor4j-timer-thread");
	}
		
	public ScheduledFuture<?> scheduleOnce(Runnable command, long delay, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.schedule(command, delay, unit) : CanceledScheduledFuture.create();
	}
	
	public ScheduledFuture<?> schedule(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit) : CanceledScheduledFuture.create();
	}
	
	@Override
	public ScheduledFuture<?> scheduleOnce(final Supplier<ActorMessage<?>> supplier, final ActorId dest, long delay, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				system.send(message.shallowCopy(dest));
			}
		}, delay, unit) : CanceledScheduledFuture.create(); 
	}
	
	@Override
	public ScheduledFuture<?> scheduleOnce(final ActorMessage<?> message, final ActorId dest, long delay, TimeUnit unit) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, dest, delay, unit);
	}
	
	@Override
	public ScheduledFuture<?> scheduleOnce(final Supplier<ActorMessage<?>> supplier, final String alias, long delay, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				system.sendViaAlias(supplier.get(), alias);
			}
		}, delay, unit) : CanceledScheduledFuture.create();
	}
	
	@Override
	public ScheduledFuture<?> scheduleOnce(final ActorMessage<?> message, final String alias, long delay, TimeUnit unit) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, alias, delay, unit);
	}
	
	@Override
	public ScheduledFuture<?> scheduleOnce(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long delay, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				for (ActorId id : group)
					system.send(message.shallowCopy(id));
			}
		}, delay, unit) : CanceledScheduledFuture.create(); 
	}
	
	@Override
	public ScheduledFuture<?> scheduleOnce(final ActorMessage<?> message, final ActorGroup group, long delay, TimeUnit unit) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, group, delay, unit);
	}
	
	@Override
	public ScheduledFuture<?> schedule(final Supplier<ActorMessage<?>> supplier, final ActorId dest, long initalDelay, long period, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				system.send(message.shallowCopy(dest));
			}
		}, initalDelay, period, unit) : CanceledScheduledFuture.create(); 
	}
	
	@Override
	public ScheduledFuture<?> schedule(final ActorMessage<?> message, final ActorId dest, long initalDelay, long period, TimeUnit unit) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, dest, initalDelay, period, unit);
	}
	
	@Override
	public ScheduledFuture<?> schedule(final Supplier<ActorMessage<?>> supplier, final String alias, long initalDelay, long period, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				system.sendViaAlias(supplier.get(), alias);
			}
		}, initalDelay, period, unit) : CanceledScheduledFuture.create(); 
	}
	
	@Override
	public ScheduledFuture<?> schedule(final ActorMessage<?> message, final String alias, long initalDelay, long period, TimeUnit unit) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, alias, initalDelay, period, unit);
	}
	
	@Override
	public ScheduledFuture<?> schedule(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long initalDelay, long period, TimeUnit unit) {
		return !timerExecutorService.isShutdown() ? timerExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				for (ActorId id : group)
					system.send(message.shallowCopy(id));
			}
		}, initalDelay, period, unit) : CanceledScheduledFuture.create(); 
	}
	
	@Override
	public ScheduledFuture<?> schedule(final ActorMessage<?> message, final ActorGroup group, long initalDelay, long period, TimeUnit unit) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, group, initalDelay, period, unit);
	}
	
	public void shutdown() {
		timerExecutorService.shutdown();
	}
}
