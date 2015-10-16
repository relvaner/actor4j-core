/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze.example;

import java.util.UUID;

import actor4j.core.Actor;
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
