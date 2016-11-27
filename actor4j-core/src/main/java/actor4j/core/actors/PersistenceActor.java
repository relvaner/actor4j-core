/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

import actor4j.core.messages.ActorMessage;
import actor4j.function.Consumer;

public abstract class PersistenceActor<S, E> extends Actor {
	@SuppressWarnings("unchecked")
	public void persist(Consumer<E> handler, E... events) {	
		cell.persist(handler, events);
	}
	
	public void saveSnapshot(Consumer<S> handler, S state) {
		cell.saveSnapshot(handler, state);
	}
	
	public void recovery(ActorMessage<?> message) {
		// empty
	}
}
