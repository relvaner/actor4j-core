/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.network;

import java.util.UUID;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.core.actors.Actor;
import actor4j.core.utils.ActorFactory;
import actor4j.server.RESTActorApplication;

@ApplicationPath("api")
public class PongApplication extends RESTActorApplication {
	@Override
	protected void configure(ActorService service) {
		service.setParallelismMin(1);
		service.setParallelismFactor(1);
		service.hardMode();
		service.addURI("http://localhost:8080/actor4j-benchmark-network-ping/api");
		
		UUID requestHandler = service.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new RequestHandler("ping");
			}
		});
		service.setAlias(requestHandler, "pong");
		System.out.println(requestHandler);
	}
}
