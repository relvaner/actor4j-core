/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.example;

import java.util.UUID;

import javax.ws.rs.ApplicationPath;

import actor4j.core.Actor;
import actor4j.core.ActorSystem;
import actor4j.core.utils.ActorFactory;
import actor4j.server.RESTActorApplication;

@ApplicationPath("api")
public class ServerApplication extends RESTActorApplication {
	@Override
	protected void configure(ActorSystem system) {
		system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		
		UUID server = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Server();
			}
		});
		system.setAlias(server, "server");
		System.out.println(server);
	}
}
