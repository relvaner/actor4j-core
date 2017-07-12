/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.actors;

import static actor4j.core.protocols.ActorProtocolTag.INTERNAL_RECOVER;

import java.util.function.Consumer;

import actor4j.core.persistence.ActorPersistenceObject;

public abstract class PersistentActor<S extends ActorPersistenceObject, E extends ActorPersistenceObject> extends Actor implements PersistenceId {
	public static final int RECOVER = INTERNAL_RECOVER;
	
	public PersistentActor() {
		super();
	}
	
	public PersistentActor(String name) {
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	public void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, E... events) {	
		cell.persist(onSuccess, onFailure, events);
	}
	
	public void saveSnapshot(Consumer<S> onSuccess, Consumer<Exception> onFailure, S state) {
		cell.saveSnapshot(onSuccess, onFailure, state);
	}
	
	public void recover(String json) {
		// empty
	}
}
