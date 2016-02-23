/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.network;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.messages.RemoteActorMessage;

public class Server extends Actor {
	public Server() {
		super("server");
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		RemoteActorMessage.optionalConvertValue(message, Payload.class);
	}
}
