/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.rest.example;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Client extends Actor {
	protected UUID next;
	protected String alias;
	
	public Client(UUID next) {
		super();
		
		this.next = next;
	}
	
	public Client(String alias) {
		super();
		
		this.alias = alias;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		System.out.println(message);
		for (int i=0; i<10; i++)
			if (next!=null)
				send(new ActorMessage<UUID>(self(), 1976+i, self(), next));
			else
				send(new ActorMessage<UUID>(self(), 1976+i, self(), null), alias);
	}
}
