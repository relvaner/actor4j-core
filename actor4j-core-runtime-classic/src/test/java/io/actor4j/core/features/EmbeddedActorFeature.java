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
package io.actor4j.core.features;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.logging.ActorLogger;
import io.actor4j.core.messages.ActorMessage;

import static io.actor4j.core.logging.ActorLogger.*;
import static org.junit.Assert.*;

public class EmbeddedActorFeature {
	protected final int SWAP=22;
	
	@Test(timeout=5000)
	public void test_become_unbecome() {
		CountDownLatch testDone = new CountDownLatch(2);
		AtomicInteger counter = new AtomicInteger(0);
		
		ActorSystem system = ActorSystem.create(AllFeaturesTest.factory());
		
		ActorId host = system.addActor(() -> new EmbeddedHostActor("host") {
			protected ActorId client;
			@Override
			public void preStart() {
				client = addEmbeddedChild(() -> new EmbeddedActor("host:client") {
					@Override
					public boolean receive(ActorMessage<?> message) {
						boolean result = false;
						
						if (message.tag() == SWAP) {
							become(msg -> {
								logger().log(DEBUG, String.format(
										"Received String message: %s", msg.valueAsString()));
								unbecome(); 
								if (counter.incrementAndGet()==1)
									assertEquals("Hello World!", msg.valueAsString());
								else
									assertEquals("Hello World Again!", msg.valueAsString());
								testDone.countDown();
								return true;
							}, false);
							result = true;
						}
						
						return result;
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (!embedded(message, client))
					unhandled(message);
			}
		});
		ActorLogger.systemLogger().setLevel(DEBUG);
		system.send(ActorMessage.create(null, SWAP, system.SYSTEM_ID(), host));
		system.send(ActorMessage.create("Hello World!", 0, system.SYSTEM_ID(), host));
		system.send(ActorMessage.create(null, SWAP, system.SYSTEM_ID(), host));
		system.send(ActorMessage.create("Hello World Again!", 0, system.SYSTEM_ID(), host));
		system.start();
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
