/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ring;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Sender extends Actor {
	protected UUID next;
	
	public Sender(UUID next) {
		super();
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		send(new ActorMessage<UUID>(self(), 0, self(), next));
	}
}
