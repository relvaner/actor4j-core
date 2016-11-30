/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.benchmark.ping.pong.grouped.bulk;

import static actor4j.benchmark.ping.pong.grouped.bulk.ActorMessageTag.*;

import actor4j.core.actors.ActorWithGroup;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Destination extends ActorWithGroup {
	public Destination(ActorGroup group) {
		super(group);
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
