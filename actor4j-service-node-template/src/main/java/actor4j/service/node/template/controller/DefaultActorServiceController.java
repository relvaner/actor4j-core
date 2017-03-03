/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.template.controller;

import actor4j.service.node.template.startup.DefaultActorService;

public class DefaultActorServiceController {
	public String isOnline() {
		return DefaultActorService.getService()!=null ? "is" : "is not";
	}
	
	public String getName() {
		return DefaultActorService.getService()!=null ? DefaultActorService.getService().getServiceNodeName() : "not available";
	}
}
