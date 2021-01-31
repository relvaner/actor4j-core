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
package io.actor4j.core.features;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.supervisor.OneForAllSupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategy;
import io.actor4j.core.supervisor.SupervisorStrategyDirective;

import static org.junit.Assert.*;
import static io.actor4j.core.logging.user.ActorLogger.logger;

public class LifeCycleFeature {
	@Test(timeout=5000)
	public void test_start() {
		CountDownLatch testDone = new CountDownLatch(2);
		
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
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
		
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
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
	public void test_stop_for_all() {
		CountDownLatch testDone = new CountDownLatch(4);
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			protected UUID child1;
			protected UUID child2;
			protected UUID child3;
			
			protected Set<UUID> waitForChildren;
			
			public SupervisorStrategy supervisorStrategy() {
				return new OneForAllSupervisorStrategy(-1, Integer.MAX_VALUE) {
					@Override
					public SupervisorStrategyDirective apply(Exception e) {
						return SupervisorStrategyDirective.STOP;
					}
					
				};
			}
			
			@Override
			public void preStart() {
				child1 = addChild(() -> new Actor("child1") {
					@Override
					public void postStop() {
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				child2 = addChild(() -> new Actor("child2") {
					@Override
					public void postStop() {
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
						throw new RuntimeException("some error in child");
					}
				});
				child3 = addChild(() -> new Actor("child3") {
					@Override
					public void postStop() {
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				waitForChildren = new HashSet<>();
				waitForChildren.add(child1);
				waitForChildren.add(child2);
				waitForChildren.add(child3);
				
				watch(child1);
				watch(child2);
				watch(child3);
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.source==system.SYSTEM_ID)
					tell(null, 0, child2);
				else if (message.tag==TERMINATED) {
					waitForChildren.remove(message.source);
					if (waitForChildren.isEmpty())
						testDone.countDown();
				}
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
	
	@Test(timeout=5000)
	public void test_stop_kill() {
		CountDownLatch testDone = new CountDownLatch(3);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
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
		
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void postStop() {
						int i = counter.incrementAndGet();
						if (i==1 || i==2) {
							logger().debug("child::postStop: "+i);
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
					logger().debug("parent::postStop: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void preRestart(Exception reason) {
				super.preRestart(reason);
				int i = counter.incrementAndGet();
				if (i==1 || i==2) {
					logger().debug("parent::preRestart: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void postRestart(Exception reason) {
				super.postRestart(reason);
				int i = counter.incrementAndGet();
				assertEquals(4, i);
				logger().debug("parent::postRestart: "+i);
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
		
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			@Override
			public void preStart() {	
				addChild(() -> new Actor("child") {
					@Override
					public void postStop() {
						int i = counter.incrementAndGet();
						if (i==1 || i==2) {
							logger().debug("child::postStop: "+i);
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
					logger().debug("parent::postStop: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void preRestart(Exception reason) {
				super.preRestart(reason);
				int i = counter.incrementAndGet();
				if (i==1 || i==2) {
					logger().debug("parent::preRestart: "+i);
					testDone.countDown();
				}
			}
			
			@Override
			public void postRestart(Exception reason) {
				super.postRestart(reason);
				int i = counter.incrementAndGet();
				assertEquals(4, i);
				logger().debug("parent::postRestart: "+i);
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
	
	@Test(timeout=5000)
	public void test_restart_for_all() {
		CountDownLatch testDone = new CountDownLatch(9);
		ActorSystem system = new ActorSystem("LifeCycleFeature");
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			protected UUID child2;
			
			public SupervisorStrategy supervisorStrategy() {
				return new OneForAllSupervisorStrategy(-1, Integer.MAX_VALUE) {
					@Override
					public SupervisorStrategyDirective apply(Exception e) {
						return SupervisorStrategyDirective.RESTART;
					}
					
				};
			}
			
			@Override
			public void preStart() {
				AtomicInteger counter = new AtomicInteger(0);
				addChild(() -> new Actor("child1") {
					@Override
					public void postStop() {
						logger().debug("child1::postStop::"+counter.get());
						if (counter.get()==1)
							assertEquals(2, counter.incrementAndGet());
						testDone.countDown();
					}
					
					@Override
					public void preRestart(Exception reason) {
						logger().debug("child1::preRestart::"+counter.get());
						assertEquals(1, counter.incrementAndGet());
						super.preRestart(reason);
						testDone.countDown();
					}
					
					@Override
					public void postRestart(Exception reason) {
						logger().debug("child1::postRestart::"+counter.get());
						assertEquals(3, counter.incrementAndGet());
						super.postRestart(reason);
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				child2 = addChild(() -> new Actor("child2") {
					@Override
					public void postStop() {
						logger().debug("child2::postStop");
						testDone.countDown();
					}
					
					@Override
					public void preRestart(Exception reason) {
						super.preRestart(reason);
						logger().debug("child2::preRestart");
						testDone.countDown();
					}
					
					@Override
					public void postRestart(Exception reason) {
						super.postRestart(reason);
						logger().debug("child2::postRestart");
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
						throw new RuntimeException("some error in child");
					}
				});
				addChild(() -> new Actor("child3") {
					@Override
					public void postStop() {
						logger().debug("child3::postStop");
						testDone.countDown();
					}
					
					@Override
					public void preRestart(Exception reason) {
						super.preRestart(reason);
						logger().debug("child3::preRestart");
						testDone.countDown();
					}
					
					@Override
					public void postRestart(Exception reason) {
						super.postRestart(reason);
						logger().debug("child3::postRestart");
						testDone.countDown();
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.source==system.SYSTEM_ID)
					tell(null, 0, child2);
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
