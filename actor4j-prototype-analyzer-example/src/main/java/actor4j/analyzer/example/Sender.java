/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.analyzer.example;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Sender extends Actor {
	protected UUID next;
	
	public Sender(String name, UUID next) {
		super(name);
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		send(message, next);
	}
}
