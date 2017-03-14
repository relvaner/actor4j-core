/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.utils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.FutureActorMessage;
import actor4j.core.pattern.actors.FutureActor;

public final class FuturePattern {
	public static <T> Future<T> ask(T value, int tag, UUID dest, Actor actor) {
		UUID mediator = actor.getSystem().addActor(() -> new FutureActor(dest, true));
		
		return ask(value, tag, dest, mediator, actor);
	}
	
	public static <T> Future<T> ask(T value, int tag, UUID dest, UUID mediator, Actor actor) {	
		CompletableFuture<T> result = new CompletableFuture<>();
		actor.send(new FutureActorMessage<T>(result, value, tag, actor.self(), mediator));
		
		return result;
	}
	
	public static <T> Future<T> ask(T value, int tag, UUID dest, ActorSystem system) {
		UUID mediator = system.addActor(() -> new FutureActor(dest, true));
		
		return ask(value, tag, dest, mediator, system);
	}
	
	public static <T> Future<T> ask(T value, int tag, UUID dest, UUID mediator, ActorSystem system) {	
		CompletableFuture<T> result = new CompletableFuture<>();
		system.send(new FutureActorMessage<T>(result, value, tag, system.SYSTEM_ID, mediator));
		
		return result;
	}
}
