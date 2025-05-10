/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
import io.actor4j.core.actors.ActorWithGroup;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;

public class UnsafeFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = ActorSystem.create(AllFeaturesTest.factory());
	}
	
	@Test(timeout=5000)
	public void test_unsafe_send() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorGroup group = new ActorGroupSet(); 
		ActorId parent = system.addActor(() -> new ActorWithGroup("parent", group) {
			protected ActorId child1;
			protected ActorId child2;
			
			@Override
			public void preStart() {	
				child1 = addChild(() -> new ActorWithGroup("child", group) {
					@Override
					public void receive(ActorMessage<?> message) {
						((InternalActorCell)cell).unsafe_send(ActorMessage.create(null, 0, self(), child2));
					}
				});
				child2 = addChild(() -> new ActorWithGroup("child", group) {
					@Override
					public void receive(ActorMessage<?> message) {
						testDone.countDown();
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				tell(null, 0, child1);
			}
		});
		
		system.start();
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_unsafe_send_alias() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorGroup group = new ActorGroupSet(); 
		ActorId parent = system.addActor(() -> new ActorWithGroup("parent", group) {
			protected ActorId child1;
			protected ActorId child2;
			protected ActorId child3;
			
			@Override
			public void preStart() {	
				child1 = addChild(() -> new ActorWithGroup("child", group) {
					@Override
					public void receive(ActorMessage<?> message) {
						((InternalActorCell)cell).unsafe_send(ActorMessage.create(null, 0, self(), child2));
					}
				});
				child2 = addChild(() -> new ActorWithGroup("child", group) {
					@Override
					public void receive(ActorMessage<?> message) {
						((InternalActorCell)cell).unsafe_send(ActorMessage.create(null, 0, self(), null), "child3");
					}
				});
				child3 = addChild(() -> new ActorWithGroup("child", group) {
					@Override
					public void receive(ActorMessage<?> message) {
						testDone.countDown();
					}
				});
				
				getSystem().setAlias(child3, "child3");
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				tell(null, 0, child1);
			}
		});
		
		system.start();
		
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), parent));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
