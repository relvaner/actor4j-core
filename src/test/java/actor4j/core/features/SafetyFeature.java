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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.safety.ErrorHandler;
import actor4j.core.utils.ActorFactory;

public class SafetyFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
	}
	
	@Test(timeout=2000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(1);
		
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
			public void handle(Throwable t, String message, UUID uuid) {
				errorHandler.handle(t, message, uuid);
				assertEquals(new NullPointerException().getMessage(), t.getMessage());
				assertEquals("actor", message);
				assertEquals(dest, uuid);
				testDone.countDown();
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
	}
	
	public static void main(String[] args) {
		SafetyFeature safetyFeature = new SafetyFeature();
		safetyFeature.before();
		safetyFeature.test();
	}
}
