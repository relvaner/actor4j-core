/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.example;

import java.util.UUID;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.core.actors.Actor;
import actor4j.core.utils.ActorFactory;
import actor4j.server.RESTActorApplication;

@ApplicationPath("api")
public class ServerApplication extends RESTActorApplication {
	@Override
	protected void configure(ActorService service) {
		service.setParallelismMin(1);
		service.setParallelismFactor(1);
		service.softMode();
		
		UUID server = service.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Server();
			}
		});
		service.setAlias(server, "server");
		System.out.println(server);
	}
}
