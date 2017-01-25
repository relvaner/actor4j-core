/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.reactive.streams;

import java.util.UUID;
import java.util.function.Consumer;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import tools4j.function.Procedure;

public class SubscriberActor extends Actor {
	protected SubscriberImpl subscriberImpl;
	
	public SubscriberActor() {
		this(null);
	}
	
	public SubscriberActor(String name) {
		super(name);
		subscriberImpl = new SubscriberImpl(this);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		subscriberImpl.receive(message);
	}
	
	public void subscribe(UUID dest, Consumer<Object> onNext, Consumer<String> onError, Procedure onComplete) {
		subscriberImpl.subscribe(dest, onNext, onError, onComplete);
	}
	
	public void unsubscribe(UUID dest) {
		subscriberImpl.unsubscribe(dest);
	}
	
	public void signalRequest(long n, UUID dest) {
		subscriberImpl.signalRequest(n, dest);
	}
}
