/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.function.Consumer;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class UnhandledActor extends Actor {
	protected Consumer<ActorMessage<?>> handler;
	
	public UnhandledActor(Consumer<ActorMessage<?>> handler) {
		this(null, handler);
	}
	
	public UnhandledActor(String name, Consumer<ActorMessage<?>> handler) {
		super(name);
		
		this.handler = handler;
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		handler.accept(message);
	}
}
