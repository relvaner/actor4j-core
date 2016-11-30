/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.benchmark.ring.nfold;

import java.util.UUID;

import actor4j.core.actors.ActorWithGroup;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Sender extends ActorWithGroup {
	protected UUID next;
	
	public Sender(ActorGroup group, UUID next) {
		super(group);
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		send(new ActorMessage<UUID>(self(), 0, self(), next));
	}
}
