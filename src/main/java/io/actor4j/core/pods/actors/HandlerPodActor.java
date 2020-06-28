/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;

public abstract class HandlerPodActor extends PodChildActor {
	protected String alias;
	protected Map<UUID, ActorMessage<?>> map;

	public HandlerPodActor(String alias, UUID groupId, PodContext context) {
		super(groupId, context);
		this.alias = alias;

		this.map = new HashMap<>();
	}
	
	@Override
	public void preStart() {
		if (context.isShard())
			setAlias(alias+context.getShardId(), false);
		else
			setAlias(alias, false);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null) {
			ActorMessage<?> originalMessage = null;
			if (message.interaction!=null)
				originalMessage = map.get(message.interaction);
			
			if (originalMessage!=null) {
				map.remove(message.interaction);
				callback(message, originalMessage, originalMessage.source);
			}
			else {
				UUID interaction = UUID.randomUUID();
				map.put(interaction, message.copy()); 
				handle(message, interaction);
			}
		}
		else
			unhandled(message);
	}
	
	public abstract void handle(ActorMessage<?> message, UUID interaction);
	public abstract void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, UUID dest);
}
