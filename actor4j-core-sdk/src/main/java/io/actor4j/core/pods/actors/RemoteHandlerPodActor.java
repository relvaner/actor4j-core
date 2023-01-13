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

import static io.actor4j.core.runtime.ActorGlobalSettings.internal_server_callback;
import static io.actor4j.core.runtime.ActorGlobalSettings.internal_server_request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;

public abstract class RemoteHandlerPodActor extends HandlerPodActor {
	protected Map<UUID, RemotePodMessage> remoteMap;
	protected Map<UUID, UUID> requestMap;

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
				requestReply = requestMap.keySet().contains(message.interaction());
		}
			
		if (remoteMessage!=null || message.value() instanceof RemotePodMessage) {
			if (remoteMessage!=null) {
				remoteMap.remove(message.interaction());
				internal_callback(message, remoteMessage);
			}
			else {
				UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
				
				if (((RemotePodMessage)message.value()).remotePodMessageDTO().reply()) {
					remoteMap.put(interaction, (RemotePodMessage)message.value()); 
					handle((RemotePodMessage)message.value(), interaction);
				}
				else
					handle((RemotePodMessage)message.value(), interaction);
			}
		}
		else if (requestReply && message.value() instanceof RemotePodMessage) {
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
	
	public void request(Object message, UUID source, UUID interaction) {
		request(message, 0, source, interaction, null);
	}
	
	public boolean request(Object message, int tag, UUID source, UUID interaction, Object params) {
		boolean result = false;
		
		if (internal_server_request!=null) {
			if (interaction!=null) { // with reply
				if (remoteMap.get(interaction)==null && !requestMap.keySet().contains(interaction)) {
					requestMap.put(interaction, source); 
					internal_server_request.accept(message, tag, source, interaction, params, self());
					result = true;
				}
			}
			else {
				internal_server_request.accept(message, tag, null, null, params, null);
				result = true;
			}
		}
		
		return result;
	}
}
