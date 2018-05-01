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
package actor4j.core.features;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

import static org.junit.Assert.*;

public class LifeCycleFeature {
	@Test(timeout=5000)
	public void test_start() {
		CountDownLatch testDone = new CountDownLatch(2);
		
		ActorSystem system = new ActorSystem();
		
		system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void preStart() {
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				testDone.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
			}
		});
		
		system.start();
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_stop() {
		CountDownLatch testDone = new CountDownLatch(3);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorSystem system = new ActorSystem();
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void postStop() {
						assertEquals(1, counter.incrementAndGet());
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
			}
			
			@Override
			public void postStop() {
				assertEquals(2, counter.incrementAndGet());
				testDone.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
			}
		});
		
		system.addActor(() -> new Actor("observer") {
			@Override
			public void preStart() {
				watch(parent);
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag==TERMINATED && message.source.equals(parent))
					testDone.countDown();
			}
		});
		
		system.start();
		
		system.send(new ActorMessage<>(null, Actor.POISONPILL, system.SYSTEM_ID, parent));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_stop_kill() {
		CountDownLatch testDone = new CountDownLatch(3);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorSystem system = new ActorSystem();
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void postStop() {
						assertEquals(1, counter.incrementAndGet());
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
			}
			
			@Override
			public void postStop() {
				assertEquals(2, counter.incrementAndGet());
				testDone.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
			}
		});
		
		system.addActor(() -> new Actor("observer") {
			@Override
			public void preStart() {
				watch(parent);
			}

			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag==TERMINATED && message.source.equals(parent))
					testDone.countDown();
			}
		});
		
		system.start();
		
		system.send(new ActorMessage<>(null, Actor.KILL, system.SYSTEM_ID, parent));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_restart() {
		CountDownLatch testDone = new CountDownLatch(4);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorSystem system = new ActorSystem();
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void postStop() {
						int i = counter.incrementAndGet();
						if (i==1 || i==2) {
							System.out.println("child::postStop: "+i);
							testDone.countDown();
						}
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				
			}
			
			@Override
			public void postStop() {
				int i = counter.incrementAndGet();
				if (i==3) {
					System.out.println("parent::postStop: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void preRestart(Exception reason) {
				super.preRestart(reason);
				int i = counter.incrementAndGet();
				if (i==1 || i==2) {
					System.out.println("parent::preRestart: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void postRestart(Exception reason) {
				super.postRestart(reason);
				int i = counter.incrementAndGet();
				assertEquals(4, i);
				System.out.println("parent::postRestart: "+i);
				testDone.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
			}
		});
		
		system.start();
		
		system.send(new ActorMessage<>(null, Actor.RESTART, system.SYSTEM_ID, parent));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_restart_exception() {
		CountDownLatch testDone = new CountDownLatch(4);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorSystem system = new ActorSystem();
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void postStop() {
						int i = counter.incrementAndGet();
						if (i==1 || i==2) {
							System.out.println("child::postStop: "+i);
							testDone.countDown();
						}
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				
			}
			
			@Override
			public void postStop() {
				int i = counter.incrementAndGet();
				if (i==3) {
					System.out.println("parent::postStop: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void preRestart(Exception reason) {
				super.preRestart(reason);
				int i = counter.incrementAndGet();
				if (i==1 || i==2) {
					System.out.println("parent::preRestart: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void postRestart(Exception reason) {
				super.postRestart(reason);
				int i = counter.incrementAndGet();
				assertEquals(4, i);
				System.out.println("parent::postRestart: "+i);
				testDone.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				throw new RuntimeException("some error");
			}
		});
		
		system.start();
		
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, parent));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
