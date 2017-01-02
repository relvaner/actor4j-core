/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.BiFunction;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class FilterActor extends PipeActor {
	public FilterActor(BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> filter, UUID next) {
		super(filter, next);
	}
	
	public FilterActor(String name, BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> filter, UUID next) {
		super(name, filter, next);
	}
}
