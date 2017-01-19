/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.BiFunction;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class BridgeActor extends PipeActor {
	public BridgeActor(BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		super(handler, next);
	}
	
	public BridgeActor(String name, BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> handler, UUID next) {
		super(name, handler, next);
	}
}
