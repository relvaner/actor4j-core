/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.network;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.messages.RemoteActorMessage;

public class RequestHandler extends Actor {
	protected String alias;
	
	public RequestHandler(String alias) {
		super();
		
		this.alias = alias;
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		RemoteActorMessage.optionalConvertValue(message, Payload.class);
		
		tell(message.value, 0, alias);
	}
}
