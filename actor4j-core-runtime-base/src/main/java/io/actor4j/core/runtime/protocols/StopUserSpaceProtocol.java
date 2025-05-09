/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.runtime.protocols.ActorProtocolTag.INTERNAL_STOP;
import static io.actor4j.core.runtime.protocols.ActorProtocolTag.INTERNAL_STOP_SUCCESS;
import static io.actor4j.core.runtime.protocols.ActorProtocolTag.INTERNAL_STOP_USER_SPACE_SUCCESS;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public final class StopUserSpaceProtocol {
	protected static void postStop(final InternalActorCell cell) {
		cell.postStop();
		cell.internal_stop();
		if (((InternalActorSystem)cell.getSystem()).isShutdownHookTriggered())
			systemPrintLog(INFO, String.format("[LIFECYCLE] actor (%s) stopped", actorLabel(cell.getActor())));
		else
			systemLogger().log(INFO, String.format("[LIFECYCLE] actor (%s) stopped", actorLabel(cell.getActor())));
	}
	
	protected static void postUserSpaceStop(final InternalActorCell cell) {
		Iterator<ActorId> iterator = cell.getDeathWatcher().iterator();
		while (iterator.hasNext()) {
			ActorId dest = iterator.next();
			((InternalActorSystem)cell.getSystem()).sendAsDirective(ActorMessage.create(null, INTERNAL_STOP_USER_SPACE_SUCCESS, cell.getId(), dest));
		}
		if (((InternalActorSystem)cell.getSystem()).isShutdownHookTriggered())
			systemPrintLog(INFO, String.format("[LIFECYCLE] actors within (%s) stopped", actorLabel(cell.getActor())));
		else
			systemLogger().log(INFO, String.format("[LIFECYCLE] actors within (%s) stopped", actorLabel(cell.getActor())));
	}
	
	public static void apply(final InternalActorCell cell) {
		final List<ActorId> waitForChildren =new ArrayList<>(cell.getChildren().size());
		
		Iterator<ActorId> iterator = cell.getChildren().iterator();
		while (iterator.hasNext()) {
			ActorId dest = iterator.next();
			cell.watch(dest);
		}
		iterator = cell.getChildren().iterator();
		while (iterator.hasNext()) {
			ActorId dest = iterator.next();
			waitForChildren.add(dest);
			((InternalActorSystem)cell.getSystem()).sendAsDirective(ActorMessage.create(null, INTERNAL_STOP, cell.getId(), dest));
		}
		
		if (waitForChildren.isEmpty()) 
			postUserSpaceStop(cell);
		else
			cell.become(new Consumer<ActorMessage<?>>() {
				protected boolean flag_stop;
				@Override
				public void accept(ActorMessage<?> message) {
					if (message.tag()==INTERNAL_STOP)
						flag_stop = true;
					else if (message.tag()==INTERNAL_STOP_SUCCESS) {
						waitForChildren.remove(message.source());
						if (waitForChildren.isEmpty()) {
							if (flag_stop)
								postStop(cell);
							else
								postUserSpaceStop(cell);
						}
					}
				}
			});
	}
}
