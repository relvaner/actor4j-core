/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.features;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import safety4j.ErrorHandler;

public class SafetyFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
	}
	
	@Test
	public void test() {
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor("SafetyFeatureActor") {
					@Override
					public void receive(ActorMessage<?> message) {
						throw new NullPointerException();
					}
					
					@Override
					public void preRestart(Exception reason) {
						super.preRestart(reason);
						assertEquals(new NullPointerException().getMessage(), reason.getMessage());
					}
					
					@Override
					public void postRestart(Exception reason) {
						super.postRestart(reason);
						assertEquals(new NullPointerException().getMessage(), reason.getMessage());
					}
				};
			}
		});
		
		ErrorHandler errorHandler = system.underlyingImpl().getExecuterService().getSafetyManager().getErrorHandler();
		system.underlyingImpl().getExecuterService().getSafetyManager().setErrorHandler(new ErrorHandler() {
			@Override
			public void handle(Exception e, String message, UUID uuid) {
				errorHandler.handle(e, message, uuid);
				assertEquals(new NullPointerException().getMessage(), e.getMessage());
				assertEquals("actor", message);
				assertEquals(dest, uuid);
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
	}
	
	public static void main(String[] args) {
		SafetyFeature safetyFeature = new SafetyFeature();
		safetyFeature.before();
		safetyFeature.test();
	}
}
