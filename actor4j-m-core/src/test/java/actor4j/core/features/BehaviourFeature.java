/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.features;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;
import actor4j.function.Consumer;

public class BehaviourFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
	}
		
	@Test
	public void test_become() {
		AtomicBoolean behaviour = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new Actor() {
			protected Consumer<ActorMessage<?>> newBehaviour = new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> t) {
					behaviour.set(true);
				}
			};
			
			@Override
			protected void receive(ActorMessage<?> message) {
				become(newBehaviour);
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour.get());
	}
	
	@Test
	public void test_become_unbecome() {
		AtomicBoolean[] behaviour = new AtomicBoolean[2];
		for (int i=0; i<behaviour.length; i++)
			behaviour[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new Actor() {
			protected Consumer<ActorMessage<?>> newBehaviour = new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> t) {
					behaviour[0].set(true);
					unbecome();
				}
			};
			
			protected boolean first = true;
			
			@Override
			protected void receive(ActorMessage<?> message) {
				if (first) {
					become(newBehaviour);
					first = false;
				}
				else
					behaviour[1].set(true);
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour[0].get());
		assertEquals(true, behaviour[1].get());
	}
	
	@Test
	public void test_stack_become_unbecome() {
		AtomicBoolean[] behaviour = new AtomicBoolean[2];
		for (int i=0; i<behaviour.length; i++)
			behaviour[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new Actor() {
			protected Consumer<ActorMessage<?>> newBehaviour1 = new Consumer<ActorMessage<?>>() {
				protected boolean first = true;
				
				@Override
				public void accept(ActorMessage<?> t) {
					if (first) {
						become(newBehaviour2, false);
						first = false;
					}
					else
						behaviour[1].set(true);
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
			protected void receive(ActorMessage<?> message) {
				become(newBehaviour1);
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour[0].get());
		assertEquals(true, behaviour[1].get());
	}
	
	@Test
	public void test_stack_become_unbecomeAll() {
		AtomicBoolean[] behaviour = new AtomicBoolean[3];
		for (int i=0; i<behaviour.length; i++)
			behaviour[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new Actor() {
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
			protected void receive(ActorMessage<?> message) {
				if (first) {
					become(newBehaviour1);
					first = false;
				}
				else
					behaviour[2].set(true);
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, behaviour[0].get());
		assertEquals(true, behaviour[1].get());
		assertEquals(true, behaviour[2].get());
	}
}
