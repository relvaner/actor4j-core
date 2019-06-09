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
package cloud.actor4j.core.features;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import cloud.actor4j.core.ActorSystem;
import cloud.actor4j.core.actors.Actor;
import cloud.actor4j.core.messages.ActorMessage;

import static org.junit.Assert.*;

public class ActorFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem();
	}
	
	@Test(timeout=5000)
	public void test_preStart_addChild() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			protected UUID child;
			
			@Override
			public void preStart() {	
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						testDone.countDown();
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				tell(null, 0, child);
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
	public void test_getActorFromPath_getActorPath() {
		AtomicReference<UUID> childA = new AtomicReference<>(null);
		AtomicReference<UUID> childB = new AtomicReference<>(null);
		AtomicReference<UUID> childBA = new AtomicReference<>(null);
		AtomicReference<UUID> childBB = new AtomicReference<>(null);
		AtomicReference<UUID> childC = new AtomicReference<>(null);
		UUID parentA = system.addActor(() -> new Actor("parentA") {
			@Override
			public void preStart() {	
				childA.set(addChild(() -> new Actor("childA") {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				}));
				childB.set(addChild(() -> new Actor("childB") {
					@Override
					public void preStart() {
						childBA.set(addChild(() -> new Actor("childBA") {
							@Override
							public void receive(ActorMessage<?> message) {
							}
						}));
						childBB.set(addChild(() -> new Actor("childBB") {
							@Override
							public void receive(ActorMessage<?> message) {
							}
						}));
					}
					@Override
					public void receive(ActorMessage<?> message) {
					}
				}));
				childC.set(addChild(() -> new Actor("childC") {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				}));
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
			}
		});
		
		AtomicReference<UUID> child = new AtomicReference<>(null);
		AtomicReference<UUID> childWithoutName = new AtomicReference<>(null);
		UUID parentB = system.addActor(() -> new Actor("parentB") {
			@Override
			public void preStart() {	
				child.set(addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				}));
				childWithoutName.set(addChild(() -> new Actor() {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				}));
			}
			@Override
			public void receive(ActorMessage<?> message) {
			}
		});
		
		system.start(() -> {
			assertEquals(null, system.getActorFromPath(null));
			assertEquals(system.USER_ID, system.getActorFromPath(""));
			assertEquals(system.USER_ID, system.getActorFromPath("/"));
			assertEquals("/", system.getActorPath(system.USER_ID));
			
			assertEquals(parentA, system.getActorFromPath("parentA"));
			assertEquals(parentA, system.getActorFromPath("/parentA"));
			assertEquals("/parentA", system.getActorPath(parentA));
			assertEquals(parentA, system.getActorFromPath(parentA.toString()));
			assertEquals(childA.get(), system.getActorFromPath("parentA/childA"));
			assertEquals("/parentA/childA", system.getActorPath(childA.get()));
			assertEquals(childB.get(), system.getActorFromPath("parentA/childB"));
			assertEquals("/parentA/childB", system.getActorPath(childB.get()));
			assertEquals(childBA.get(), system.getActorFromPath("parentA/childB/childBA"));
			assertEquals("/parentA/childB/childBA", system.getActorPath(childBA.get()));
			assertEquals(childBB.get(), system.getActorFromPath("parentA/childB/childBB"));
			assertEquals("/parentA/childB/childBB", system.getActorPath(childBB.get()));
			assertEquals(childC.get(), system.getActorFromPath("parentA/childC"));
			assertEquals("/parentA/childC", system.getActorPath(childC.get()));
			
			assertEquals(parentB, system.getActorFromPath("parentB"));
			assertEquals(parentB, system.getActorFromPath("/parentB"));
			assertEquals(parentB, system.getActorFromPath(parentB.toString()));
			assertEquals("/parentB", system.getActorPath(parentB));
			assertEquals(child.get(), system.getActorFromPath("parentB/child"));
			assertEquals(child.get(), system.getActorFromPath("/parentB/child"));
			assertEquals(child.get(), system.getActorFromPath(parentB.toString()+"/"+child.toString()));
			assertEquals("/parentB/child", system.getActorPath(child.get()));
			
			assertEquals("/parentB/"+childWithoutName.toString(), system.getActorPath(childWithoutName.get()));
		}, null);
		system.shutdownWithActors(true);
	}
}
