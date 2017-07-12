/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.hub;

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
