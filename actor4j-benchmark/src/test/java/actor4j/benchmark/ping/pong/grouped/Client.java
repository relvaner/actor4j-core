/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.benchmark.ping.pong.grouped;

import static actor4j.benchmark.ping.pong.grouped.ActorMessageTag.MSG;

import java.util.UUID;

import actor4j.core.actors.ActorWithGroup;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Client extends ActorWithGroup {
	protected UUID dest;
	
	public Client(ActorGroup group, UUID dest) {
		super(group);
		
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
