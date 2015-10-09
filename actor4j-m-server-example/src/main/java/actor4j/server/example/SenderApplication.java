/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.example;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorFactory;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;
import actor4j.server.RESTActorClientRunnable;

public class SenderApplication {
	public SenderApplication() {
		ActorSystem system = new ActorSystem(true);
		configure(system);
		system.setClientRunnable(new RESTActorClientRunnable(system.getServerURIs(), system.getParallelismMin()*system.getParallelismFactor(), 10000));
		system.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		system.shutdown();
	}
	
	protected static class Payload {
		public List<String> data;
		
		public Payload() {
			data = new ArrayList<>();
		}
	}
	
	protected void configure(ActorSystem system) {
		system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		system.addURI("http://localhost:8080/actor4j-m-server-example/rest");
		//system.addURI("http://192.168.0.100:8080/actor4j-m-server-example/rest");
		
		//UUID sender = system.addActor(new Sender(UUID.fromString("490a452e-d53f-41b5-b740-7eada0ae372f")));
		UUID sender = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Sender("receiver");
			}
		});
		Payload payload = new Payload();
		payload.data.add("Hello");
		payload.data.add("World");
		payload.data.add("!");
		UUID helloWorld = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor() {
					@Override
					protected void receive(ActorMessage<?> message) {
						this.send(message, "receiver");
					}
				};
			}
		});
		
		//system.send(new ActorMessage<Object>(null, 0, sender, sender));
		system.send(new ActorMessage<Object>(payload, 0, helloWorld, helloWorld));
	}
	
	public static void main(String[] args) {
		new SenderApplication();
	}
}
