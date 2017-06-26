/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
