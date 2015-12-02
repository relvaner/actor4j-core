/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong.grouped.bulk;

import static actor4j.benchmark.ping.pong.grouped.bulk.ActorMessageTag.*;

import java.util.UUID;

import actor4j.core.actors.ActorGroupMember;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Client extends ActorGroupMember {
	protected UUID dest;
	
	protected long initalMessages;
	
	public Client(ActorGroup group, UUID dest) {
		super(group);
		
		this.dest = dest;
		
		initalMessages = 100;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==MSG.ordinal()) {
			message.source = self();
			message.dest = dest;
			send(message);
		}
		else if (message.tag==RUN.ordinal())
            for (int i=0; i<initalMessages; i++) {
            	send(new ActorMessage<Object>(null, MSG, self(), dest));
		}
	}
}
