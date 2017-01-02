/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.BiFunction;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class PipeActor extends Actor {
	protected BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler;
	protected UUID next;
	
	public PipeActor(BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		this(null, handler, next);
	}
	
	public PipeActor(String name, BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		super(name);
		
		this.handler = handler;
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (handler!=null)
			send(handler.apply(this, message), next);
	}
}
