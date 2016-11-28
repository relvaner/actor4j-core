/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

import java.util.UUID;

import actor4j.core.persistence.ActorPersistenceObject;
import actor4j.function.Consumer;

public abstract class PersistenceActor<S extends ActorPersistenceObject, E extends ActorPersistenceObject> extends Actor {
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
	
	public void recovery(String json) {
		// empty
	}
	
	public abstract UUID persistenceId();
}
