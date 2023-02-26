/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.embedded.protocol;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import io.actor4j.core.actors.EmbeddedActor;
import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.exceptions.ActorInitializationException;
import io.actor4j.core.runtime.embedded.InternalEmbeddedActorCell;

public class RestartProtocol {
	protected final InternalEmbeddedActorCell cell;

	public RestartProtocol(InternalEmbeddedActorCell cell) {
		this.cell = cell;
	}

	protected void postRestart(Exception reason) {
		if (cell.host() instanceof EmbeddedHostActor h) {
			cell.postStop();
			try {
				EmbeddedActor newEmbeddedActor = (EmbeddedActor)h.underlyingImpl().getContainer().getInstance(cell.getId());
				newEmbeddedActor.setCell(cell);
				cell.setActor(newEmbeddedActor);
				cell.postRestart(reason);
				systemLogger().log(INFO, String.format("[LIFECYCLE] embedded actor (%s) restarted", actorLabel(cell.getActor())));
			} catch (Exception e) {
				throw new ActorInitializationException(); // never must occur
			}
		}
	}
	
	public void apply(final Exception reason) {
		postRestart(reason);
	}
}
