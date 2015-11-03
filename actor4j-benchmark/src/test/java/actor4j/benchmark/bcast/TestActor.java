/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.bcast;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.HubPattern;

public class TestActor extends Actor {
	protected HubPattern hub;
	
	public TestActor(ActorGroup group) {
		super();
		
		hub = new HubPattern(this, group);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		message.source = self();
		hub.broadcast(message);
	}
}
