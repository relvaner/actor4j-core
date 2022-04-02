/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
import io.actor4j.core.pods.RemotePodMessage;

import static io.actor4j.core.internal.ActorEnvironmentSettings.internal_server_callback;
import static io.actor4j.core.internal.ActorEnvironmentSettings.internal_server_request;

public abstract class RemoteHandlerPodActor extends HandlerPodActor {
	protected Map<UUID, RemotePodMessage> remoteMap;
	protected Map<UUID, Object> requestMap;

	public RemoteHandlerPodActor(String alias, UUID groupId, PodContext context) {
		super(alias, groupId, context);
		
		this.remoteMap = new HashMap<>();
		this.requestMap = new HashMap<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		RemotePodMessage remoteMessage = null;
		Object requestMessage = null;
		if (message.interaction()!=null) {
			remoteMessage = remoteMap.get(message.interaction());
			if (remoteMessage==null)
				requestMessage = requestMap.get(message.interaction());
		}
			
		if (remoteMessage!=null || message.value() instanceof RemotePodMessage) {
			if (remoteMessage!=null) {
				remoteMap.remove(message.interaction());
				internal_callback(message, remoteMessage);
			}
			else {
				UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
				remoteMap.put(interaction, (RemotePodMessage)message.value()); 
				handle((RemotePodMessage)message.value(), interaction);
			}
		}
		else if (requestMessage!=null && message.value() instanceof RemotePodMessage) {
			requestMap.remove(message.interaction());
			handle((RemotePodMessage)message.value(), message.interaction());
		}
		else
			super.receive(message);
	}
	
	protected void internal_callback(ActorMessage<?> message, RemotePodMessage remoteMessage) {
		Object result = callback(message, remoteMessage);
		if (remoteMessage.remotePodMessageDTO().reply() && internal_server_callback!=null)
			internal_server_callback.accept(remoteMessage.replyAddress(), result, message.tag());
	}

	public abstract void handle(RemotePodMessage remoteMessage, UUID interaction);
	public abstract Object callback(ActorMessage<?> message, RemotePodMessage remoteMessage);
	
	public void request(Object message) {
		request(message, null);
	}
	
	public boolean request(Object message, UUID interaction) {
		boolean result = false;
		
		if (internal_server_request!=null) {
			if (interaction!=null) {
				if (remoteMap.get(interaction)==null && requestMap.get(interaction)==null) {
					requestMap.put(interaction, message); 
					internal_server_request.accept(message, interaction, context.domain());
					result = true;
				}
			}
			else {
				internal_server_request.accept(message, null, null);
				result = true;
			}
		}
		
		return result;
	}
}
