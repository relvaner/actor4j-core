/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence;

import java.util.UUID;

public class ActorPersistenceObject {
	public UUID persistenceId;

	public ActorPersistenceObject() {
		super();
	}

	@Override
	public String toString() {
		return "ActorPersistenceObject [persistenceId=" + persistenceId + "]";
	}
}
