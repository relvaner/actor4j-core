/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

import actor4j.core.messages.ActorMessage;
import actor4j.function.Consumer;

public abstract class PersistenceActor<S, E> extends Actor {
	public PersistenceActor(String name) {
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	public void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, E... events) {	
		cell.persist(onSuccess, onFailure, events);
	}
	
	public void saveSnapshot(Consumer<S> onSuccess, Consumer<Exception> onFailure, S state) {
		cell.saveSnapshot(onSuccess, onFailure, state);
	}
	
	public void recovery(ActorMessage<?> message) {
		// empty
	}
}
