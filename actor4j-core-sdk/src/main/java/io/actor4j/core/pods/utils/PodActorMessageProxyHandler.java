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
package io.actor4j.core.pods.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.InternalActorSystem;

public abstract class PodActorMessageProxyHandler {
	protected final ActorRef host;
	protected final UUID groupId;
	
	protected final Map<UUID, ActorMessage<?>> map;

	public PodActorMessageProxyHandler(ActorRef host, UUID groupId) {
		super();
		this.host = host;
		this.groupId = groupId;
		
		this.map = new HashMap<>();
	}

	public void apply(ActorMessage<?> message) {
		ActorMessage<?> originalMessage = null;
		if (message.interaction()!=null)
			originalMessage = map.get(message.interaction());
		
		if (originalMessage!=null) {
			map.remove(message.interaction());
			callback(message, originalMessage, originalMessage.source(), originalMessage.interaction());
		}
		else if (messagefromPod(message)) {
			((Actor)host).unhandled(message);
			unhandled(message);
		}
		else if (message.interaction()!=null && message.interaction().equals(ActorMessage.NO_REPLY)) {
			handle(message, ActorMessage.NO_REPLY);
		}
		else
		{
			UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
			map.put(interaction, message.copy()); 
			handle(message, interaction);
//			UUID interaction = UUID.randomUUID();
//			map.put(interaction, message.copy()); 
//			handle(message, interaction);
		}
	}
	
	public abstract void handle(ActorMessage<?> message, UUID interaction);
	public abstract void unhandled(ActorMessage<?> message);
	public abstract void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, ActorId dest, UUID interaction);
	
	public boolean messagefromPod(ActorMessage<?> message) {
		boolean result = false;
		
		InternalActorCell cell = ((InternalActorSystem)host.getSystem()).getCells().get(message.source());
		if (cell!=null) {
			Actor actor = cell.getActor();
			if (actor!=null && actor instanceof ActorGroupMember)
				result = ((ActorGroupMember)actor).getGroupId().equals(groupId);
		}
		
		return result;
	}
}
