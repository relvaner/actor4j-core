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
package io.actor4j.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public abstract class ActorMessageProxyHandler {
	protected final Map<UUID, ActorMessage<?>> map;

	public ActorMessageProxyHandler() {
		super();

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
		else if (message.interaction()!=null && message.interaction().equals(ActorMessage.NO_REPLY)) {
			handle(message, ActorMessage.NO_REPLY);
		}
		else
		{
			UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
			map.put(interaction, message.copy()); 
			handle(message, interaction);
		}
	}
	
	public abstract void handle(ActorMessage<?> message, UUID interaction);
	public abstract void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, ActorId dest, UUID interaction);
}
