/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.BiFunction;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class TranslateActor extends PipeActor {
	public TranslateActor(BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> translate, UUID next) {
		super(translate, next);
	}
	
	public TranslateActor(String name, BiFunction<Actor, ActorMessage<?>, ActorMessage<?>> translate, UUID next) {
		super(name, translate, next);
	}
}
