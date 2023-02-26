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

import static io.actor4j.core.logging.ActorLogger.INFO;
import static io.actor4j.core.logging.ActorLogger.systemLogger;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import io.actor4j.core.actors.EmbeddedHostActor;
import io.actor4j.core.runtime.embedded.InternalEmbeddedActorCell;

public class StopProtocol {
	protected final InternalEmbeddedActorCell cell;

	public StopProtocol(InternalEmbeddedActorCell cell) {
		this.cell = cell;
	}
	
	public void postStop() {
		if (cell.host() instanceof EmbeddedHostActor h)
			h.removeEmbeddedChild(cell.getId());
		systemLogger().log(INFO, String.format("[LIFECYCLE] embedded actor (%s) stopped", actorLabel(cell.getActor())));
	}

	public void apply() {
		postStop();
	}
}
