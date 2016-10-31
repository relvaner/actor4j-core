/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.example.rest;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.service.example.startup.ExampleActorService;
import actor4j.service.rest.RESTActorService;

@ApplicationPath("api")
public class ExampleRESTActorService extends RESTActorService {
	@Override
	public ActorService getService() {
		return ExampleActorService.getService();
	}
}
