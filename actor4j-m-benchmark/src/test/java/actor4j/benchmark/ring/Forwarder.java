/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ring;

import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;

public class Forwarder extends Actor {
	protected UUID next;
	
	protected long initalMessages;
	
	public Forwarder() {
		super();
	}
	
	public Forwarder(UUID next) {
		super();
		
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
