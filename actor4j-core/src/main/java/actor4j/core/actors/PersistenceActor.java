/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

import static actor4j.core.protocols.ActorProtocolTag.INTERNAL_RECOVER;

import java.util.UUID;
import java.util.function.Consumer;

import actor4j.core.persistence.ActorPersistenceObject;

public abstract class PersistenceActor<S extends ActorPersistenceObject, E extends ActorPersistenceObject> extends Actor {
	public static final int RECOVER = INTERNAL_RECOVER;
	
	public PersistenceActor() {
		super();
	}
	
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
