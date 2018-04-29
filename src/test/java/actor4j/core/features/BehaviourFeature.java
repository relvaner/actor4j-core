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
package actor4j.core.features;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;

public class BehaviourFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
	}
		
	@Test(timeout=5000)
	public void test_become() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		final AtomicBoolean behaviour = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected Consumer<ActorMessage<?>> newBehaviour = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							behaviour.set(true);
							testDone.countDown();
						}
					};
					
					@Override
					public void receive(ActorMessage<?> message) {
						become(newBehaviour);
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour.get());
	}
	
	@Test(timeout=5000)
	public void test_become_unbecome() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		final AtomicBoolean[] behaviour = new AtomicBoolean[2];
		for (int i=0; i<behaviour.length; i++)
			behaviour[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected Consumer<ActorMessage<?>> newBehaviour = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							behaviour[0].set(true);
							unbecome();
						}
					};
					
					protected boolean first = true;
					
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							become(newBehaviour);
							first = false;
						}
						else {
							behaviour[1].set(true);
							testDone.countDown();
						}
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour[0].get());
		assertEquals(true, behaviour[1].get());
	}
	
	@Test(timeout=5000)
	public void test_stack_become_unbecome() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		final AtomicBoolean[] behaviour = new AtomicBoolean[2];
		for (int i=0; i<behaviour.length; i++)
			behaviour[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected Consumer<ActorMessage<?>> newBehaviour1 = new Consumer<ActorMessage<?>>() {
						protected boolean first = true;
						
						@Override
						public void accept(ActorMessage<?> t) {
							if (first) {
								become(newBehaviour2, false);
								first = false;
							}
							else {
								behaviour[1].set(true);
								testDone.countDown();
							}
						}
					};
					
					protected Consumer<ActorMessage<?>> newBehaviour2 = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							behaviour[0].set(true);
							unbecome();
						}
					};
					
					@Override
					public void receive(ActorMessage<?> message) {
						become(newBehaviour1);
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour[0].get());
		assertEquals(true, behaviour[1].get());
	}
	
	@Test(timeout=5000)
	public void test_stack_become_unbecomeAll() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		final AtomicBoolean[] behaviour = new AtomicBoolean[3];
		for (int i=0; i<behaviour.length; i++)
			behaviour[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected Consumer<ActorMessage<?>> newBehaviour1 = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							behaviour[0].set(true);
							become(newBehaviour2, false);
						}
					};
					
					protected Consumer<ActorMessage<?>> newBehaviour2 = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							behaviour[1].set(true);
							unbecomeAll();
						}
					};
					
					protected boolean first = true;
					
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							become(newBehaviour1);
							first = false;
						}
						else {
							behaviour[2].set(true);
							testDone.countDown();
						}
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour[0].get());
		assertEquals(true, behaviour[1].get());
		assertEquals(true, behaviour[2].get());
	}
}
