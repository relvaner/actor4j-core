/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.benchmark.ring.nfold.bulk;

import java.util.UUID;

import actor4j.core.actors.ActorWithGroup;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Forwarder extends ActorWithGroup {
	protected UUID next;
	
	protected long initalMessages;
	
	public Forwarder(ActorGroup group) {
		super(group);
	}
	
	public Forwarder(ActorGroup group, UUID next) {
		super(group);
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (next!=null)
			send(message, next);
		else 
			send(message, message.valueAsUUID());
	}
}
