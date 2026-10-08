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

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ResourceActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public class ResourceActorFeature {
	protected static class StatelessResourceActor extends ResourceActor {
		public StatelessResourceActor() {
			super();
		}

		public StatelessResourceActor(String name) {
			super(name);
		}

		@Override
		public void receive(ActorMessage<?> message) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			tell(null, 1, message.source());
		}
	}
	
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = ActorSystem.create(AllFeaturesTest.factory());
	}
	
	@Test(timeout=5000)
	public void test_stateless() {
		CountDownLatch testDone = new CountDownLatch(5);
		
		ActorId resource = system.addActor(() -> new StatelessResourceActor("resource"));
		ActorId parent = system.addActor(() -> new Actor("parent") {
			protected ActorId child;
			
			@Override
			public void preStart() {	
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						if (message.tag()==1 && message.source().equals(resource))
							testDone.countDown();
					}
				});
			}
			@Override
			public void receive(ActorMessage<?> message) {
				send(ActorMessage.create(null, 0, child, resource));
			}
		});
		
		system.start();
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_stateful() {
		CountDownLatch testDone = new CountDownLatch(5);
		
		ActorId resource = system.addActor(() -> new ResourceActor("resource", true) {
			@Override
			public void receive(ActorMessage<?> message) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				tell(null, 1, message.source());
			}
		});
		
		ActorId parent = system.addActor(() -> new Actor("parent") {
			protected ActorId child;
			
			@Override
			public void preStart() {	
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						if (message.tag()==1 && message.source().equals(resource))
							testDone.countDown();
					}
				});
			}
			@Override
			public void receive(ActorMessage<?> message) {
				send(ActorMessage.create(null, 0, child, resource));
			}
		});
		
		system.start();
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
