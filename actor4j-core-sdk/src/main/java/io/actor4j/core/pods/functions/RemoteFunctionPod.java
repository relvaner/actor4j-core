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
package io.actor4j.core.pods.functions;

import static io.actor4j.core.logging.ActorLogger.ERROR;
import static io.actor4j.core.logging.ActorLogger.systemLogger;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.ActorPod;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.pods.utils.PodStatus;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.config.InternalServerCallback;
import io.actor4j.core.utils.Reply;

public abstract class RemoteFunctionPod extends ActorPod {
	@Override
	public PodActor create() {
		return new PodActor() {
			protected Map<UUID, ActorMessage<?>> pendingHandler;
			protected PodRemoteFunction podRemoteFunction;
			
			@Override
			public void preStart() {
				pendingHandler = new HashMap<>();
				
				if (getContext().isShard())
					setAlias(domain()+getContext().shardId());
				else
					setAlias(domain());
				
				register();
			}
			
			public void handleReply(ActorMessage<?> message, Reply result, UUID interaction) {
				result = result!=null ? result : Reply.none();
				
				if (!result.isPending()) {
					ActorMessage<?> originMessage = result.interaction()!=null ? pendingHandler.remove(result.interaction()) : null;
					ActorMessage<?> messageToProcess = originMessage!=null ? originMessage : message;
					
					if (messageToProcess.value() instanceof RemotePodMessage remoteMessage) {
						if (remoteMessage.remotePodMessageDTO().reply()) {
							if (result.isDone())
								internal_callback(this, remoteMessage, result);
							else
								internal_callback(this, remoteMessage, handleRejectedReply(result));
						}
					}
					else if (result.isDone())
						internal_callback(this, messageToProcess, result);	
				}
				else { // result.isPending()
					if (interaction!=null)
						pendingHandler.putIfAbsent(interaction, message);
					else
						systemLogger().log(ERROR, String.format("Pending reply without interaction from actor (%s)", actorLabel(this)));
				}
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				Reply result = null;
				
				if (message.value() instanceof RemotePodMessage remoteMessage) {
					UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
					result = podRemoteFunction.handle(remoteMessage, interaction);
					handleReply(message, result, interaction);
				}
				else {
					result = podRemoteFunction.handle(message);
					handleReply(message, result, message.interaction());
				}
			}
			
			@Override
			public void register() {
				podRemoteFunction = createFunction(this, getContext());
			}				
		};
	}
	
	protected void internal_callback(ActorRef host, ActorMessage<?> message, Reply result) {
		host.tell(result.value(), result.tag(), message.source(), message.interaction(), message.protocol(), message.domain());
	}
	
	protected void internal_callback(ActorRef host, RemotePodMessage remoteMessage, Reply result) {
		InternalServerCallback internalServerCallback = ((InternalActorSystem)host.getSystem()).getRuntimeConfig().internalServerCallback();
		
		if (remoteMessage.remotePodMessageDTO().reply() && internalServerCallback!=null)
			internalServerCallback.accept(remoteMessage.replyAddress(), result.value(), result.tag());
	}
	
	public abstract PodRemoteFunction createFunction(ActorRef host, PodContext context);
	
	public Reply handleRejectedReply(Reply result) {
		return Reply.of(result.value(), PodStatus.CONFLICT);
	}
}
