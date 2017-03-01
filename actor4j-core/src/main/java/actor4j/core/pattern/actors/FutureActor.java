/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.messages.FutureActorMessage;

public class FutureActor extends Actor {
	protected CompletableFuture<Object> future;
	protected UUID source;
	protected UUID destination;
	
	public FutureActor() {
		super();
	}

	public FutureActor(String name) {
		super(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive(ActorMessage<?> message) {
		if (message instanceof FutureActorMessage<?>) {
			source = message.source;
			destination = message.dest;
			future = ((FutureActorMessage<Object>)message).future;
			tell(message.value, message.tag, message.dest);
		}
		else if (message.source==destination)
			tell(future.complete(message.value), message.tag, source);
		else
			unhandled(message);
	}

}
