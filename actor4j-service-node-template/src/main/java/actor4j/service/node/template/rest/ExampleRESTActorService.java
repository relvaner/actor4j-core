/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.service.node.template.rest;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.service.node.rest.RESTActorService;
import actor4j.service.node.template.startup.ExampleActorService;


@ApplicationPath("api")
public class ExampleRESTActorService extends RESTActorService {
	@Override
	public ActorService getService() {
		return ExampleActorService.getService();
	}
}
