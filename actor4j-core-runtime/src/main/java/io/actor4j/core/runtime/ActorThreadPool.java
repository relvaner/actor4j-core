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
package io.actor4j.core.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import io.actor4j.core.messages.ActorMessage;

public class ActorThreadPool extends AbstractActorExecutionUnitPool<ActorThread> {
	protected final CountDownLatch countDownLatch;
	
	public ActorThreadPool(DefaultInternalActorRuntimeSystem system) {
		super(system, new ActorThreadPoolHandler(system));
		
		countDownLatch = new CountDownLatch(system.getConfig().parallelism()*system.getConfig().parallelismFactor());
		DefaultActorThreadFactory defaultActorThreadFactory = new DefaultActorThreadFactory(system.getConfig().name());
		for (int i=0; i<system.getConfig().parallelism()*system.getConfig().parallelismFactor(); i++) {
			try {
				ActorThread t = defaultActorThreadFactory.newThread(system);
				t.onTermination = new Runnable() {
					@Override
					public void run() {
						countDownLatch.countDown();
					}
				};
				executionUnitList.add(t);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		((DefaultActorExecutionUnitPoolHandler<ActorThread>)executionUnitPoolHandler).beforeStart(executionUnitList);
		for (ActorThread t : executionUnitList)
			t.start();
	}
	
	public void shutdown(Runnable onTermination, boolean await) {
		if (executionUnitList.size()>0) {
			for (ActorThread t : executionUnitList)
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
					
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						Thread.currentThread().interrupt();
//					}
					
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
		return (ActorThreadPoolHandler)executionUnitPoolHandler;
	}
	
	public boolean postInnerOuter(ActorMessage<?> message, UUID source) {
		return getActorThreadPoolHandler().postInnerOuter(message, source);
	}
	
	public boolean postOuter(ActorMessage<?> message) {
		return getActorThreadPoolHandler().postOuter(message);
	}
	
	public boolean postQueue(ActorMessage<?> message, BiConsumer<ActorThread, ActorMessage<?>> biconsumer) {
		return getActorThreadPoolHandler().postQueue(message, biconsumer);
	}
	
	public void postPersistence(ActorMessage<?> message) {
		getActorThreadPoolHandler().postPersistence(message);
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executionUnitList)
			list.add(t.getInnerQueue().size());
		return list;
	}
	
	public List<Integer> getWorkerOuterQueueSizes() {
		List<Integer> list = new ArrayList<>();
		for (ActorThread t : executionUnitList)
			list.add(t.getOuterQueue().size());
		return list;
	}
}
