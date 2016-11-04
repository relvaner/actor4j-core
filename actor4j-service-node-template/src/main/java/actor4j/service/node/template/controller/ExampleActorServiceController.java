package actor4j.service.node.template.controller;

import actor4j.service.node.template.startup.ExampleActorService;

public class ExampleActorServiceController {
	public String isOnline() {
		return ExampleActorService.getService()!=null ? "is" : "is not";
	}
	
	public String getName() {
		return ExampleActorService.getService()!=null ? ExampleActorService.getService().getServiceNodeName() : "not available";
	}
}
