/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.bcast2;

import java.util.Random;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroupAsList;
import actor4j.core.utils.HubPattern;

public class TestActor extends Actor {
	protected ActorGroupAsList group;
	protected HubPattern hub;
	
	public TestActor(ActorGroupAsList group) {
		super();
		
		this.group = group;
		hub = new HubPattern(this);
	}
	
	@Override
	public void preStart() {
		Random random = new Random();
		long size = 2;
		long count = 0;
		while (count!=size) {
			UUID next = group.get(random.nextInt(group.size()));
			if (!hub.contains(next)) {
				hub.add(next);
				count++;
			}
		}
	}

	@Override
	public void receive(ActorMessage<?> message) {
		message.source = self();
		hub.broadcast(message);
	}
}
