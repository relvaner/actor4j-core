/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods.functions.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.Reply;

public class FunctionPodProxyHandler {
	protected final ActorRef host;
	protected final PodFunctionHandler functionHandler;
	
	protected final Map<UUID, ActorMessage<?>> pendingHandler;

	public FunctionPodProxyHandler(ActorRef host, PodFunctionHandler functionHandler) {
		super();
		this.host = host;
		this.functionHandler = functionHandler;
		
		this.pendingHandler = new HashMap<>();
	}

	protected void handleReply(ActorMessage<?> message, Reply result, UUID interaction) {
		if (result!=null) {
			if (result.isDone()) {
				ActorMessage<?> originalMessage = result.interaction()!=null ? pendingHandler.remove(result.interaction()) : null;
				ActorMessage<?> callbackMessage = originalMessage!=null ? originalMessage : message;
					
				if (!ActorMessage.NO_REPLY.equals(callbackMessage.interaction()))
					callback(host, callbackMessage, result);
			}
			else if (result.isPending())
				pendingHandler.putIfAbsent(interaction, message);
		}
	}

	public void apply(ActorMessage<?> message) {
		UUID interaction = message.interaction()!=null && !ActorMessage.NO_REPLY.equals(message.interaction()) ? message.interaction() : UUID.randomUUID();
		
		Reply result = functionHandler.handle(message, interaction, !ActorMessage.NO_REPLY.equals(message.interaction()));
		handleReply(message, result, interaction);
	}
	
	protected void callback(ActorRef host, ActorMessage<?> message, Reply result) {
		host.tell(result.value(), result.tag(), message.source(), message.interaction(), message.protocol(), message.domain());
	}
}
