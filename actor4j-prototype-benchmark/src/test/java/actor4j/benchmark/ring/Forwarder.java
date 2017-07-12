/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ring;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Forwarder extends Actor {
	protected UUID next;
	
	protected long initalMessages;
	
	public Forwarder() {
		super();
	}
	
	public Forwarder(UUID next) {
		super();
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (next!=null)
			send(message, next);
		else 
			send(message, message.valueAsUUID());
	}
}
