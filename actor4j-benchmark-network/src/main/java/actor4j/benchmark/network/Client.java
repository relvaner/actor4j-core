/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.network;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Client extends Actor {
	protected UUID next;
	protected String alias;
	
	public Client(UUID next) {
		super("client");
		
		this.next = next;
	}
	
	public Client(String alias) {
		super();
		
		this.alias = alias;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		for (int i=0; i<message.tag; i++)
			tell(message, 0, alias);
	}
}
