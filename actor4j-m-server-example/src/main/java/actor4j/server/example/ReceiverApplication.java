/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.example;

import java.util.UUID;

import javax.ws.rs.ApplicationPath;

import actor4j.core.Actor;
import actor4j.core.ActorCreator;
import actor4j.core.ActorSystem;
import actor4j.server.RESTActorApplication;

@ApplicationPath("rest")
public class ReceiverApplication extends RESTActorApplication {
	@Override
	protected void configure(ActorSystem system) {
		system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		
		UUID receiver = system.addActor(new ActorCreator() {
			@Override
			public Actor create() {
				return new Receiver();
			}
		});
		system.setAlias(receiver, "receiver");
		System.out.println(receiver);
	}
}
