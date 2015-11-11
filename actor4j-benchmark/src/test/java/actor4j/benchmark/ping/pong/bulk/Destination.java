/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong.bulk;

import static actor4j.benchmark.hub.ActorMessageTag.MSG;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Destination extends Actor {
	public Destination() {
		super();
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==MSG.ordinal()) {
			message.dest = message.source;
			message.source = self();
			send(message);
		}
	}
}
