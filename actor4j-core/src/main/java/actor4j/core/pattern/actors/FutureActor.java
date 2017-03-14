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
	protected UUID dest;
	
	protected boolean stopOnComplete;
	
	public FutureActor(UUID dest, boolean stopOnComplete) {
		this(null, dest, stopOnComplete);
	}

	public FutureActor(String name, UUID dest, boolean stopOnComplete) {
		super(name);
		this.dest = dest;
		this.stopOnComplete = stopOnComplete;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive(ActorMessage<?> message) {
		if (message instanceof FutureActorMessage<?>) {
			source = message.source;
			future = ((FutureActorMessage<Object>)message).future;
			tell(message.value, message.tag, dest);
		}
		else if (message.source==dest) {
			tell(future.complete(message.value), message.tag, source);
			if (stopOnComplete)
				stop();
		}
		else
			unhandled(message);
	}
}