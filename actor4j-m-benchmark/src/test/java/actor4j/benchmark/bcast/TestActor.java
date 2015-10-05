/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.bcast;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;
import actor4j.core.HubPattern;

public class TestActor extends Actor {
	protected HubPattern hub;
	
	public TestActor(HubPattern hub) {
		super();
		
		this.hub = hub;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		message.source = getId();
		hub.broadcast(message);
	}
}
