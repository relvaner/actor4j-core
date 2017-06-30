/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
