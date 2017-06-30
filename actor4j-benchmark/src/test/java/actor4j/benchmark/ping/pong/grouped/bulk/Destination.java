/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
