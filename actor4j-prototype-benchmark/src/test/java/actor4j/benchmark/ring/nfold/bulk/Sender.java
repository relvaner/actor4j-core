/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ring.nfold.bulk;

import static actor4j.benchmark.ring.nfold.bulk.ActorMessageTag.MSG;
import static actor4j.benchmark.ring.nfold.bulk.ActorMessageTag.RUN;

import java.util.UUID;

import actor4j.core.actors.ActorWithGroup;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Sender extends ActorWithGroup {
	protected UUID next;
	
	protected long initalMessages;
	
	public Sender(ActorGroup group, UUID next) {
		super(group);
		
		this.next = next;
		
		initalMessages = 100;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (message.tag==MSG.ordinal())
			send(new ActorMessage<UUID>(self(), 0, self(), next));
		else if (message.tag==RUN.ordinal())
			 for (int i=0; i<initalMessages; i++)
				 send(new ActorMessage<UUID>(self(), MSG, self(), next));
	}
}
