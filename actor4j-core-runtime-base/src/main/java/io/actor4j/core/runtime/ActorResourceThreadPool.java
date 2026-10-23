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
package io.actor4j.core.runtime;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Pool for resource actors: an idle thread takes the task. While all threads are busy, the pool grows up to
// maximumPoolSize. Only then tasks are queued (unbounded). Nothing is rejected while running.
public class ActorResourceThreadPool extends ThreadPoolExecutor {
	// tasks handed over and not finished yet (running or queued)
	protected final AtomicInteger submitted;

	public ActorResourceThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new TaskQueue(), threadFactory, new TaskQueuePolicy());
		submitted = new AtomicInteger();
		((TaskQueue)getQueue()).pool = this;
	}

	public int submitted() {
		return submitted.get();
	}

	@Override
	public void execute(Runnable command) {
		submitted.incrementAndGet();
		try {
			super.execute(command);
		}
		catch (RejectedExecutionException e) {
			submitted.decrementAndGet();
			throw e;
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		submitted.decrementAndGet();
		super.afterExecute(r, t);
	}

	// refuses a task while no thread is idle and the pool may still grow, so ThreadPoolExecutor starts a new thread
	protected static class TaskQueue extends LinkedBlockingQueue<Runnable> {
		protected static final long serialVersionUID = 1L;

		protected transient volatile ActorResourceThreadPool pool;

		@Override
		public boolean offer(Runnable task) {
			ActorResourceThreadPool pool = this.pool;
			if (pool==null)
				return super.offer(task);
			int threads = pool.getPoolSize();
			// submitted counts this task too: not more tasks than threads means one of them is idle
			if (pool.submitted.get()<=threads || threads>=pool.getMaximumPoolSize())
				return super.offer(task);
			return false;
		}

		// queues without the check above
		protected boolean force(Runnable task) {
			return super.offer(task);
		}
	}

	// the queue refused, but another task took the last free place in the pool meanwhile: queue it after all
	protected static class TaskQueuePolicy implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
			if (executor.isShutdown())
				throw new RejectedExecutionException("resource pool is shut down");
			// cannot fail with the unbounded queue; if it ever does, the caller hears of it instead of losing the task
			if (!((TaskQueue)executor.getQueue()).force(task))
				throw new RejectedExecutionException("resource pool queue refused the task");
		}
	}
}
