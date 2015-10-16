/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.bcast;

import actor4.core.utils.HubPattern;
import actor4j.core.Actor;
import actor4j.core.ActorGroup;
import actor4j.core.messages.ActorMessage;

public class TestActor extends Actor {
	protected HubPattern hub;
	
	public TestActor(ActorGroup group) {
		super();
		
		hub = new HubPattern(this, group);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		message.source = getId();
		hub.broadcast(message);
	}
}
