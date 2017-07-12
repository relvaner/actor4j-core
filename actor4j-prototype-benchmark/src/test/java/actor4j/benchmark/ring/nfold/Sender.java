/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ring.nfold;

import java.util.UUID;

import actor4j.core.actors.ActorWithGroup;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class Sender extends ActorWithGroup {
	protected UUID next;
	
	public Sender(ActorGroup group, UUID next) {
		super(group);
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		send(new ActorMessage<UUID>(self(), 0, self(), next));
	}
}
