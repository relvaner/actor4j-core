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
import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.actors.PersistentActor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.persistence.ActorPersistenceObject;
import actor4j.core.persistence.Recovery;

import static actor4j.core.utils.ActorLogger.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.MongoClient;

import static org.junit.Assert.*;

public class PersistenceFeature {
	static class MyState extends ActorPersistenceObject {
		public String title;
		
		public MyState() {
			super();
		}

		public MyState(String title) {
			super();
			this.title = title;
		}

		@Override
		public String toString() {
			return "MyState [persistenceId=" + persistenceId + ", timeStamp=" + timeStamp + ", title=" + title + "]";
		}
	}
	
	static class MyEvent extends ActorPersistenceObject {
		public String title;
		
		public MyEvent() {
			super();
		}

		public MyEvent(String title) {
			super();
			this.title = title;
		}

		@Override
		public String toString() {
			return "MyEvent [persistenceId=" + persistenceId + ", timeStamp=" + timeStamp + ", title=" + title + "]";
		}
	}
	
	@Test(timeout=5000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(2);
		
		ActorSystem system = new ActorSystem("ExamplePersistence");
		
		AtomicBoolean first = new AtomicBoolean(true);
		UUID id = system.addActor(() -> new PersistentActor<MyState, MyEvent>("example") {
			@Override
			public void receive(ActorMessage<?> message) {
				MyEvent event1 = new MyEvent("I am the first event!");
				MyEvent event2 = new MyEvent("I am the second event!");
				
				saveSnapshot(null, null, new MyState("I am a state!"));
				
				persist(
					(s) -> logger().debug(String.format("Event: %s", s)), 
					(e) -> logger().error(String.format("Error: %s", e.getMessage())),
					event1, event2);
				
				if (first.getAndSet(false))
					tell(null, Actor.RESTART, self());
			}

			@Override
			public void recover(String json) {
				if (!Recovery.isError(json)) {
					logger().debug(String.format("Recovery: %s", json));
					Recovery<MyState, MyEvent> obj = Recovery.convertValue(json, new TypeReference<Recovery<MyState, MyEvent>>(){});
					logger().debug(String.format("Recovery: %s", obj.toString()));
					if (first.get())
						assertEquals("{\"state\":{}}", json);
					else {
						assertEquals("I am a state!", obj.state.title);
						assertTrue(obj.events.size()==2);
						assertEquals("I am the first event!", obj.events.get(0).title);
						assertEquals("I am the second event!", obj.events.get(1).title);
					}
					testDone.countDown();
				}
				else
					logger().error(String.format("Error: %s", Recovery.getErrorMsg(json)));
			}
			
			@Override
			public UUID persistenceId() {
				/* e.g. https://www.uuidgenerator.net/ */
				return UUID.fromString("60f086af-27d3-44e9-8fd7-eb095c98daed");
			}
		});
		
		// Drop database
		MongoClient client = new MongoClient("localhost", 27017);
		client.dropDatabase("actor4j-test");
		client.close();
		
		system.persistenceMode("localhost", 27017, "actor4j-test");
		system.start();
		
		system.sendWhenActive(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, id));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
