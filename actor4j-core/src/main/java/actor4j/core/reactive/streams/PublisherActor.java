/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.reactive.streams;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class PublisherActor extends Actor {
	protected PublisherImpl publisherImpl;
	
	public PublisherActor() {
		this(null);
	}
	
	public PublisherActor(String name) {
		super(name);
		publisherImpl = new PublisherImpl(this);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		publisherImpl.receive(message);
	}
	
	public <T> void broadcast(T value) {
		publisherImpl.broadcast(value);
	}
	
	public <T> boolean signalOnNext(T value, UUID dest) {
		return publisherImpl.signalOnNext(value, dest);
	}
	
	public void signalOnError(String error, UUID dest) {
		publisherImpl.signalOnError(error, dest);
	}
	
	public void signalOnComplete(UUID dest) {
		publisherImpl.signalOnComplete(dest);
	}
}
