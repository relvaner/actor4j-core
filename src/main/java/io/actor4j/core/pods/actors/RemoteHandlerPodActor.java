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

import io.actor4j.core.function.TriConsumer;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;

public abstract class RemoteHandlerPodActor extends HandlerPodActor {
	public static TriConsumer<String, Object, Integer> internal_server_callback;
	
	protected Map<UUID, RemotePodMessage> remoteMap;

	public RemoteHandlerPodActor(String alias, UUID groupId, PodContext context) {
		super(alias, groupId, context);
		
		this.remoteMap = new HashMap<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		RemotePodMessage remoteMessage = null;
		if (message.interaction!=null)
			remoteMessage = remoteMap.get(message.interaction);
			
		if (remoteMessage!=null || message.value instanceof RemotePodMessage) {
			if (remoteMessage!=null) {
				remoteMap.remove(message.interaction);
				internal_callback(message, remoteMessage);
			}
			else {
				UUID interaction = message.interaction!=null ? message.interaction : UUID.randomUUID();
				remoteMap.put(interaction, (RemotePodMessage)message.value); 
				handle((RemotePodMessage)message.value, interaction);
			}
		}
		else
			super.receive(message);
	}
	
	protected void internal_callback(ActorMessage<?> message, RemotePodMessage remoteMessage) {
		Object result = callback(message, remoteMessage);
		if (remoteMessage.remotePodMessageDTO.reply && internal_server_callback!=null)
			internal_server_callback.accept(remoteMessage.replyAddress, result, message.tag);
	}

	public abstract void handle(RemotePodMessage remoteMessage, UUID interaction);
	public abstract Object callback(ActorMessage<?> message, RemotePodMessage remoteMessage);
}
