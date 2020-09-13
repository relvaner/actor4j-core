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
package io.actor4j.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import io.actor4j.core.messages.ActorMessage;

public class ActorThreadPool {
	protected final ActorSystemImpl system;
	
	protected final List<ActorThread> actorThreads;
	protected final ActorThreadPoolHandler actorThreadPoolHandler;
	
	protected final CountDownLatch countDownLatch;
	
	public ActorThreadPool(ActorSystemImpl system) {
		this.system = system;
		
		actorThreads = new ArrayList<>();
		actorThreadPoolHandler = new ActorThreadPoolHandler(system);
		
		countDownLatch = new CountDownLatch(system.parallelismMin*system.parallelismFactor);
		ActorThreadFactory actorThreadFactory = new ActorThreadFactory(system.name);
		for (int i=0; i<system.parallelismMin*system.parallelismFactor; i++) {
			try {
				ActorThread t = actorThreadFactory.newThread(system);
				t.onTermination = new Runnable() {
					@Override
					public void run() {
						countDownLatch.countDown();
					}
				};
				actorThreads.add(t);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		actorThreadPoolHandler.beforeStart(actorThreads);
		for (ActorThread t : actorThreads)
			t.start();
	}
	
	public void shutdown(Runnable onTermination, boolean await) {
		if (actorThreads.size()>0) {
			for (ActorThread t : actorThreads)
				t.interrupt();
		}
		
		if (onTermination!=null || await) {
			Thread waitOnTermination = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						countDownLatch.await();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					
					if (onTermination!=null)
						onTermination.run();
				}
			});
			
			waitOnTermination.start();
			if (await)
				try {
					waitOnTermination.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
		}
	}
	
	public ActorThreadPoolHandler getActorThreadPoolHandler() {
		return actorThreadPoolHandler;
	}
	
	public boolean postInnerOuter(ActorMessage<?> message, UUID source) {
		return actorThreadPoolHandler.postInnerOuter(message, source);
	}
	
	public boolean postOuter(ActorMessage<?> message) {
		return actorThreadPoolHandler.postOuter(message);
	}
	
	public boolean postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		return actorThreadPoolHandler.postQueue(message, biconsumer);
	}
	
	public void postPersistence(ActorMessage<?> message) {
		actorThreadPoolHandler.postPersistence(message);
	}
	
	public long getCount() {
		long sum = 0;
		for (ActorThread t : actorThreads)
			sum += t.getCount();
		
		return sum;
	}
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		for (ActorThread t : actorThreads)
			list.add(t.getCount());
		return list;
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : actorThreads)
			list.add(t.getInnerQueue().size());
		return list;
	}
	
	public List<Integer> getWorkerOuterQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : actorThreads)
			list.add(t.getOuterQueue().size());
		return list;
	}
}
