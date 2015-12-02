/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong.grouped;

import static actor4j.benchmark.ping.pong.grouped.ActorMessageTag.MSG;

import actor4j.core.actors.ActorGroupMember;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Destination extends ActorGroupMember {
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
