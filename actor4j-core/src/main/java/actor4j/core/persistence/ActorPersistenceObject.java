/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence;

import java.util.Date;
import java.util.UUID;

public class ActorPersistenceObject {
	public UUID persistenceId;
	public Date timeStamp;

	public ActorPersistenceObject() {
		super();
		timeStamp = new Date();
	}

	@Override
	public String toString() {
		return "ActorPersistenceObject [persistenceId=" + persistenceId + ", timeStamp=" + timeStamp + "]";
	}
}
