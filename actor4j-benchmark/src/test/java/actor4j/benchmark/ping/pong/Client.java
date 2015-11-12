/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong;

import static actor4j.benchmark.ping.pong.ActorMessageTag.MSG;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Client extends Actor {
	protected UUID dest;
	
	public Client(UUID dest) {
		super();
		
		this.dest = dest;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==MSG.ordinal()) {
			message.source = self();
			message.dest = dest;
			send(message);
		}
	}
}
