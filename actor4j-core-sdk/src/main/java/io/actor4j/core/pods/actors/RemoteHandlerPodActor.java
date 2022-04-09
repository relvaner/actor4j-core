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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;

import static io.actor4j.core.internal.ActorGlobalSettings.internal_server_callback;
import static io.actor4j.core.internal.ActorGlobalSettings.internal_server_request;

public abstract class RemoteHandlerPodActor extends HandlerPodActor {
	protected Map<UUID, RemotePodMessage> remoteMap;
	protected Set<UUID> requestSet;

	public RemoteHandlerPodActor(String alias, UUID groupId, PodContext context) {
		super(alias, groupId, context);
		
		this.remoteMap = new HashMap<>();
		this.requestSet = new HashSet<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		RemotePodMessage remoteMessage = null;
		boolean requestReply = false;
		if (message.interaction()!=null) {
			remoteMessage = remoteMap.get(message.interaction());
			if (remoteMessage==null)
				requestReply = requestSet.contains(message.interaction());
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
		else if (requestReply && message.value() instanceof RemotePodMessage) {
			requestSet.remove(message.interaction());
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
		request(message, 0, null);
	}
	
	public void request(Object message, int tag) {
		request(message, tag, null);
	}
	
	public void request(Object message, UUID interaction) {
		request(message, 0, interaction);
	}
	
	public boolean request(Object message, int tag, UUID interaction) {
		boolean result = false;
		
		if (internal_server_request!=null) {
			if (interaction!=null) { // with reply
				if (remoteMap.get(interaction)==null && !requestSet.contains(interaction)) {
					requestSet.add(interaction); 
					internal_server_request.accept(message, tag, interaction, context.domain());
					result = true;
				}
			}
			else {
				internal_server_request.accept(message, tag, null, context.domain());
				result = true;
			}
		}
		
		return result;
	}
}
