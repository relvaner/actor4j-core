/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.features;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.function.Consumer;

public class AwaitFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
	}
		
	@Test
	public void test_await() {
		final AtomicBoolean[] postconditions = new AtomicBoolean[2];
		for (int i=0; i<postconditions.length; i++)
			postconditions[i] = new AtomicBoolean(false);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor() {
					protected Consumer<ActorMessage<?>> action = new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> t) {
							postconditions[0].set(true);
						}
					};
					
					protected boolean first = true;
					
					@Override
					public void receive(ActorMessage<?> message) {
						if (first) {
							await(1, action);
							first = false;
						}
						else
							postconditions[1].set(true);
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 1, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 1, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
		
		assertEquals(true, postconditions[0].get());
		assertEquals(true, postconditions[1].get());
	}
}
