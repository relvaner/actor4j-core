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
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.functions.PodRemoteFunction;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.config.InternalServerCallback;
import io.actor4j.core.utils.Reply;

public class RemoteFunctionPodProxyHandler {
	protected final ActorRef host;
	protected final PodRemoteFunction podRemoteFunction;
	
	protected final Map<UUID, ActorMessage<?>> pendingHandler;

	public RemoteFunctionPodProxyHandler(ActorRef host, PodRemoteFunction podRemoteFunction) {
		super();
		this.host = host;
		this.podRemoteFunction = podRemoteFunction;
		
		this.pendingHandler = new HashMap<>();
	}
	
	protected void handleReply(ActorMessage<?> message, Reply result, UUID interaction) {
		result = result!=null ? result : Reply.none();
		
		if (!result.isPending()) {
			ActorMessage<?> originalMessage = result.interaction()!=null ? pendingHandler.remove(result.interaction()) : null;
			ActorMessage<?> callbackMessage = originalMessage!=null ? originalMessage : message;
			
			if (callbackMessage.value() instanceof RemotePodMessage remoteMessage) {
				if (remoteMessage.remotePodMessageDTO().reply()) {
					if (result.isDone())
						callback(host, remoteMessage, result);
					else
						callback(host, remoteMessage, handleRejectedReply(result));
				}
			}
			else if (result.isDone()) {
				if (!ActorMessage.NO_REPLY.equals(callbackMessage.interaction()))
					callback(host, callbackMessage, result);
			}
		}
		else // result.isPending()
			pendingHandler.putIfAbsent(interaction, message);
	}

	public void apply(ActorMessage<?> message) {
		Reply result = null;

		UUID interaction = message.interaction()!=null && !ActorMessage.NO_REPLY.equals(message.interaction()) ? message.interaction() : UUID.randomUUID();
		if (message.value() instanceof RemotePodMessage remoteMessage)
			result = podRemoteFunction.handle(remoteMessage, interaction, remoteMessage.remotePodMessageDTO().reply());
		else
			result = podRemoteFunction.handle(message, interaction, !ActorMessage.NO_REPLY.equals(message.interaction()));
		handleReply(message, result, interaction);
	}
	
	protected void callback(ActorRef host, ActorMessage<?> message, Reply result) {
		host.tell(result.value(), result.tag(), message.source(), message.interaction(), message.protocol(), message.domain());
	}
	
	protected void callback(ActorRef host, RemotePodMessage remoteMessage, Reply result) {
		InternalServerCallback internalServerCallback = ((InternalActorSystem)host.getSystem()).getRuntimeConfig().internalServerCallback();
		
		if (remoteMessage.remotePodMessageDTO().reply() && internalServerCallback!=null)
			internalServerCallback.accept(remoteMessage.replyAddress(), result.value(), result.tag());
	}
	
	protected Reply handleRejectedReply(Reply result) {
		return Reply.of(result.value(), PodStatus.CONFLICT);
	}
}
