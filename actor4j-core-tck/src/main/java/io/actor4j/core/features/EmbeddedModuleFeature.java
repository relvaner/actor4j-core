/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
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

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.modules.AbstractEmbeddedModule;
import io.actor4j.core.modules.EmbeddedModule;

import static org.junit.Assert.*;

public class EmbeddedModuleFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = ActorSystem.create(AllFeaturesTest.factory());
	}
	
	@Test(timeout=5000)
	public void test_module() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorId host = system.addActor(() -> new Actor("test-host") {
			protected EmbeddedModule module;
			
			@Override
			public void preStart() {	
				module = new AbstractEmbeddedModule("test-module", this, null) {
					@Override
					protected boolean onMatch(ActorMessage<?> message) {
						assertEquals(42, message.valueAsInt());
						testDone.countDown();
						
						return true;
					}
				};
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				module.match(message);
			}
		});
		
		system.start();
		
		system.send(ActorMessage.create(42, 0, system.SYSTEM_ID(), host));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_module_failure() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorId host = system.addActor(() -> new Actor("test-host") {
			protected EmbeddedModule module;
			
			@Override
			public void preStart() {	
				module = new AbstractEmbeddedModule("test-module", this, null) {
					@Override
					protected boolean onMatch(ActorMessage<?> message) {
						throw new NullPointerException();
					}
					
					@Override
					public void fallback(ActorMessage<?> message, Exception e) {
						System.out.println("doSomething()");
						testDone.countDown();
					}
				};
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				module.match(message);
			}
		});
		
		system.start();
		
		system.send(ActorMessage.create(42, 0, system.SYSTEM_ID(), host));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
