/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.protocols;

import actor4j.core.ActorCell;
import actor4j.core.actors.PersistenceActor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.persistence.actor.PersistenceServiceActor;

public class RecoveryProtocol {
	protected final ActorCell cell;

	public RecoveryProtocol(ActorCell cell) {
		this.cell = cell;
	}
	
	public void apply() {
		if (cell.getSystem().isPersistenceMode() && cell.getActor() instanceof PersistenceActor) {
			cell.getSystem().getMessageDispatcher().postPersistence(
					new ActorMessage<String>(((PersistenceActor<?,?>)cell.getActor()).persistenceId().toString(), PersistenceServiceActor.RECOVER, cell.getId(), null));}
	}
}
