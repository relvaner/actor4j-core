/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.protocols;

import actor4j.core.ActorCell;
import actor4j.core.actors.PersistentActor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.persistence.actor.PersistenceServiceActor;

public class RecoverProtocol {
	protected final ActorCell cell;

	public RecoverProtocol(ActorCell cell) {
		this.cell = cell;
	}
	
	public void apply() {
		if (cell.getSystem().isPersistenceMode() && cell.getActor() instanceof PersistentActor) {
			cell.setActive(false);
			cell.getSystem().getMessageDispatcher().postPersistence(
					new ActorMessage<String>(((PersistentActor<?,?>)cell.getActor()).persistenceId().toString(), PersistenceServiceActor.RECOVER, cell.getId(), null));
		}
	}
}
