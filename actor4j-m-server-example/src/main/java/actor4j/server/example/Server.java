/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.server.example;

import java.util.ArrayList;
import java.util.List;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;
import actor4j.core.RemoteActorMessage;

public class Server extends Actor {
	public Server() {
		super();
	}

	protected static class Payload {
		public List<String> data;
		
		public Payload() {
			data = new ArrayList<>();
		}

		@Override
		public String toString() {
			return "Payload [data=" + data + "]";
		}
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message instanceof RemoteActorMessage)
			message.value = ((RemoteActorMessage) message).convertValue(Payload.class);
		
		System.out.println(message);
	}
}
