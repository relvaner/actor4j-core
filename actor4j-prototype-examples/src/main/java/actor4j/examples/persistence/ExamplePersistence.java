/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.examples.persistence;
import actor4j.core.ActorSystem;
import actor4j.core.actors.PersistentActor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.persistence.ActorPersistenceObject;
import actor4j.core.persistence.Recovery;

import static actor4j.core.utils.ActorLogger.*;

import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;

public class ExamplePersistence {
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
	
	public ExamplePersistence() {
		ActorSystem system = new ActorSystem("ExamplePersistence");
		
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
			}

			@Override
			public void recover(String json) {
				if (!Recovery.isError(json)) {
					logger().debug(String.format("Recovery: %s", json));
					Recovery<MyState, MyEvent> obj = Recovery.convertValue(json, new TypeReference<Recovery<MyState, MyEvent>>(){});
					logger().debug(String.format("Recovery: %s", obj.toString()));
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
		
		system.persistenceMode("localhost", 27017, "actor4j");
		system.start();
		
		system.sendWhenActive(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, id));
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
	
	public static void main(String[] args) {
		new ExamplePersistence();
	}
}
