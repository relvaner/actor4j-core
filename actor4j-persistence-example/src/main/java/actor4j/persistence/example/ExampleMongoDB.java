/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.persistence.example;

import java.util.Date;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.persistence.ActorPersistence;

public class ExampleMongoDB {
	public ExampleMongoDB() {
		ActorSystem system = new ActorSystem("ExampleMongoDB");
		
		system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Actor("MyActor") {
					@Override
					public void preStart() {
						ActorPersistence.saveSnapshot(null, new ExampleState(self().toString(), name, new Date(), 0));
					}
					
					@Override
					public void receive(ActorMessage<?> message) {
					}
				};
			}
		});
		
		system.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
		
	}
	
	public static void main(String[] args) {
		new ExampleMongoDB();
	}
}
