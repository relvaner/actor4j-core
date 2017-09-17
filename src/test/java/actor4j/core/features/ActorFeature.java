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
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

import static org.junit.Assert.*;

public class ActorFeature {
	@Test
	public void test_preStart_addChild() {
		ActorSystem system = new ActorSystem();
		
		AtomicBoolean done = new AtomicBoolean(false);
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			protected UUID child;
			
			@Override
			public void preStart() {	
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						done.set(true);
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
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(done.get());
		
		system.shutdownWithActors(true);
	}
}
