/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.bcast;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.HubPattern;

public class TestActor extends Actor {
	protected HubPattern hub;
	
	public TestActor(ActorGroup group) {
		super();
		
		hub = new HubPattern(this, group);
	}

	@Override
	public void receive(ActorMessage<?> message) {
		message.source = self();
		hub.broadcast(message);
	}
}
