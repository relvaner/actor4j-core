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

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.PseudoActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

import static io.actor4j.core.logging.user.ActorLogger.logger;
import static org.junit.Assert.*;

public class PseudoActorFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem();
	}
	
	@Test(timeout=5000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(10);
		
		PseudoActor main = new PseudoActor(system, false) {
			@Override
			public void receive(ActorMessage<?> message) {
			}
		};
		
		final int[] postconditions_numbers = new int[] { 341, 351, 451, 318, 292, 481, 240, 478, 382, 502, 158, 401, 438, 353, 165, 344, 6, 9, 18, 31, 77, 90, 45, 63, 190, 1 };
		
		UUID numberGenerator = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor("numberGenerator") {
					protected ScheduledFuture<?> timerFuture;
					protected int counter = 0;
					
					@Override
					public void preStart() {
						timerFuture = system.timer()
							.schedule(() -> new ActorMessage<Integer>(postconditions_numbers[counter++], 0, self(), null), main.getId(), 0, 25, TimeUnit.MILLISECONDS);
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
						logger().debug(String.format("numberGenerator received a message.tag (%d) from main", message.tag));
						testDone.countDown();
					}
					
					@Override
					public void postStop() {
						timerFuture.cancel(true);
					}
				};
			}
		});
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			protected int i;
			protected int counter = 0;
			@Override
			public void run() {
				main.stream()
					.skip(counter).limit(1)
					.forEach(msg -> { 
						assertEquals(postconditions_numbers[counter], msg.valueAsInt()); 
						logger().debug("-> main received a message.value ("+msg.valueAsInt()+") from numberGenerator");
					});
				counter++;
					
				main.send(new ActorMessage<>(null, i++, main.getId(), numberGenerator));
			}
		}, 0, 50);
		
		system.start();
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_await() {
		ActorSystem system = new ActorSystem();
		
		PseudoActor main = new PseudoActor(system, true) {
			@Override
			public void receive(ActorMessage<?> message) {
			}
		};
		
		system.start();
		
		system.send(new ActorMessage<Boolean>(true, 0, system.SYSTEM_ID, main.getId()));
		ActorMessage<?> message1 = main.await();
		assertEquals(true, message1.valueAsBoolean());

		system.shutdownWithActors(true);
	}
}