/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.template.rest;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.service.node.rest.RESTActorService;
import actor4j.service.node.template.startup.DefaultActorService;


@ApplicationPath("api")
public class DefaultRESTActorService extends RESTActorService {
	@Override
	public ActorService getService() {
		return DefaultActorService.getService();
	}
}
