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

package io.actor4j.core.pods.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.runtime.config.InternalServerCallback;
import io.actor4j.core.runtime.config.InternalServerRequest;

public abstract class RemoteHandlerPodActor extends HandlerPodActor {
	protected Map<UUID, RemotePodMessage> remoteMap;
	protected Map<UUID, ActorId> requestMap;

	public RemoteHandlerPodActor(String alias, UUID groupId, PodContext context) {
		super(alias, groupId, context);
		
		this.remoteMap = new HashMap<>();
		this.requestMap = new HashMap<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		RemotePodMessage remoteMessage = null;
		boolean requestReply = false;
		if (message.interaction()!=null) {
			remoteMessage = remoteMap.get(message.interaction());
			if (remoteMessage==null)
				requestReply = requestMap.containsKey(message.interaction());
		}
			
		if (requestReply && message.value() instanceof RemotePodMessage) {
			requestMap.remove(message.interaction());
			handle((RemotePodMessage)message.value(), message.interaction());
		}
		else if (remoteMessage!=null) {
			remoteMap.remove(message.interaction());
			internal_callback(message, remoteMessage);
		}
		else if (message.value() instanceof RemotePodMessage) {
			UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
			
			if (((RemotePodMessage)message.value()).remotePodMessageDTO().reply())
				remoteMap.put(interaction, (RemotePodMessage)message.value()); 
			handle((RemotePodMessage)message.value(), interaction);
		}
		else
			super.receive(message);
	}
	
	protected void internal_callback(ActorMessage<?> message, RemotePodMessage remoteMessage) {
		InternalServerCallback internalServerCallback = ((InternalActorSystem)getSystem()).getRuntimeConfig().internalServerCallback();
		
		Object result = callback(message, remoteMessage);
		if (remoteMessage.remotePodMessageDTO().reply() && internalServerCallback!=null)
			internalServerCallback.accept(remoteMessage.replyAddress(), result, message.tag());
	}

	public abstract void handle(RemotePodMessage remoteMessage, UUID interaction);
	public abstract Object callback(ActorMessage<?> message, RemotePodMessage remoteMessage);
	
	public void request(Object message) {
		request(message, 0, null, null, null);
	}
	
	public void request(Object message, int tag) {
		request(message, tag, null, null, null);
	}
	
	public void request(Object message, int tag, Object params) {
		request(message, tag, null, null, params);
	}
	
	public void request(Object message, UUID interaction) {
		request(message, 0, null, interaction, null);
	}
	
	public void request(Object message, int tag, UUID interaction) {
		request(message, tag, null, interaction, null);
	}
	
	public void request(Object message, ActorId source, UUID interaction) {
		request(message, 0, source, interaction, null);
	}
	
	public boolean request(Object message, int tag, ActorId source, UUID interaction, Object params) {
		boolean result = false;
		
		InternalServerRequest internalServerRequest = ((InternalActorSystem)getSystem()).getRuntimeConfig().internalServerRequest();
		if (internalServerRequest!=null) {
			if (interaction!=null) { // with reply
				if (remoteMap.get(interaction)==null && !requestMap.keySet().contains(interaction)) {
					requestMap.put(interaction, source); 
					internalServerRequest.accept(message, tag, source, interaction, params, self());
					result = true;
				}
			}
			else {
				internalServerRequest.accept(message, tag, null, null, params, null);
				result = true;
			}
		}
		
		return result;
	}
}
