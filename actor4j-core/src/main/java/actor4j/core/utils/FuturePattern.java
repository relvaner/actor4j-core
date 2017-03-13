/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.utils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import actor4j.core.actors.Actor;
import actor4j.core.messages.FutureActorMessage;
import actor4j.core.pattern.actors.FutureActor;

public final class FuturePattern {
	public static <T> Future<T> ask(T value, int tag, UUID dest, Actor actor) {
		UUID mediator = actor.getSystem().addActor(() -> new FutureActor(dest, true));
		
		CompletableFuture<T> result = new CompletableFuture<>();
		actor.send(new FutureActorMessage<T>(result, value, tag, actor.self(), mediator));
		
		return result;
	}
}
