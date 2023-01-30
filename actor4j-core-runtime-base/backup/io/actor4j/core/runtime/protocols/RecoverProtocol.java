/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.runtime.protocols;

import io.actor4j.core.actors.PersistentActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.persistence.actor.PersistenceServiceActor;

public class RecoverProtocol {
	protected final InternalActorCell cell;

	public RecoverProtocol(InternalActorCell cell) {
		this.cell = cell;
	}
	
	public void apply() {
		if (cell.getSystem().getConfig().persistenceMode() && cell.getActor() instanceof PersistentActor) {
			cell.setActive(false);
			((InternalActorSystem)cell.getSystem()).getMessageDispatcher().postPersistence(
				ActorMessage.create(((PersistentActor<?,?>)cell.getActor()).persistenceId().toString(), PersistenceServiceActor.RECOVER, cell.getId(), null));
		}
	}
}
