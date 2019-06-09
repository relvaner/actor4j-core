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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import cloud.actor4j.core.ActorSystem;
import cloud.actor4j.core.actors.Actor;
import cloud.actor4j.core.messages.ActorMessage;
import cloud.actor4j.core.utils.FuturePattern;

import static org.junit.Assert.*;

public class FuturePatternFeature {
	@Test(timeout=5000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(2);
		
		ActorSystem system = new ActorSystem();
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			protected Future<String> future;
			protected UUID child;
			
			@Override
			public void preStart() {
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						if (message.tag==11)
							tell("success_two", 11, message.source);
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag==10) {
					tell("success", 0, message.source);
					future = FuturePattern.ask(null, 11, child, this);
					become((msg) -> {
						// don't block the actor itself
						if (future.isDone())
							try {
								assertEquals("success_two", future.get());
								unbecome();
								testDone.countDown();
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							}
					});
					getSystem().timer().schedule(new ActorMessage<>("test", 0, self(), null), self(), 0, 100, TimeUnit.MILLISECONDS);
					testDone.countDown();
				}
			}
		});
		
		system.start();
		try {
			assertEquals("success", FuturePattern.ask(null, 10, parent, system).get());
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
		try {
			testDone.await();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
