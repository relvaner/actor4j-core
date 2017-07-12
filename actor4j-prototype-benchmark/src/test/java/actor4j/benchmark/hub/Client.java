/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.hub;

import static actor4j.benchmark.hub.ActorMessageTag.MSG;
import static actor4j.benchmark.hub.ActorMessageTag.RUN;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Client extends Actor {
	protected UUID dest;
	
	protected long initalMessages;
	
	public Client(UUID dest) {
		super();
		
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
