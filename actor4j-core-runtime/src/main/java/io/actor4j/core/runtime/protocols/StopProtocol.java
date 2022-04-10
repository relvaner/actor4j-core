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

import static io.actor4j.core.internal.protocols.ActorProtocolTag.*;
import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import io.actor4j.core.internal.InternalActorCell;
import io.actor4j.core.internal.InternalActorSystem;
import io.actor4j.core.messages.ActorMessage;

public class StopProtocol {
	protected final InternalActorCell cell;

	public StopProtocol(InternalActorCell cell) {
		this.cell = cell;
	}
	
	protected void postStop() {
		cell.postStop();
		cell.internal_stop();
		systemLogger().log(INFO, String.format("[LIFECYCLE] actor (%s) stopped", actorLabel(cell.getActor())));
	}
	
	public void apply() {
		final List<UUID> waitForChildren =new ArrayList<>(cell.getChildren().size());
		
		Iterator<UUID> iterator = cell.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			cell.watch(dest);
		}
		iterator = cell.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			waitForChildren.add(dest);
			((InternalActorSystem)cell.getSystem()).sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, cell.getId(), dest));
		}
		
		if (waitForChildren.isEmpty()) 
			postStop();
		else
			cell.become(new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					if (message.tag()==INTERNAL_STOP_SUCCESS) {
						waitForChildren.remove(message.source());
						if (waitForChildren.isEmpty())
							postStop();
					}
				}
			});
	}
}
