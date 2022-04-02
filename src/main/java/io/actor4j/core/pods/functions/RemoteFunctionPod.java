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
package io.actor4j.core.pods.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.ActorPod;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.utils.Pair;

import static io.actor4j.core.internal.ActorEnvironmentSettings.internal_server_callback;
import static io.actor4j.core.internal.ActorEnvironmentSettings.internal_server_request; // TODO:

public abstract class RemoteFunctionPod extends ActorPod {
	@Override
	public PodActor create() {
		return new PodActor() {
			protected PodRemoteFunction podRemoteFunction;
			
			protected Map<UUID, RemotePodMessage> remoteMap;
			
			@Override
			public void preStart() {
				remoteMap = new HashMap<>();
				
				if (getContext().isShard())
					setAlias(domain()+getContext().shardId());
				else
					setAlias(domain());
				
				register();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				RemotePodMessage remoteMessage = null;
				if (message.interaction()!=null)
					remoteMessage = remoteMap.get(message.interaction());
				
				if (remoteMessage!=null || message.value() instanceof RemotePodMessage) {
					Pair<Object, Integer> result = null;
					if (remoteMessage!=null) {
						result = podRemoteFunction.handle(message);
						if (result!=null) {
							remoteMap.remove(message.interaction());
							internal_callback(remoteMessage, result);
						}	
					}
					else {
						UUID interaction = message.interaction()!=null ? message.interaction() : UUID.randomUUID();
						result = podRemoteFunction.handle((RemotePodMessage)message.value(), interaction);
						if (result!=null)
							internal_callback((RemotePodMessage)message.value(), result);
						else
							remoteMap.put(interaction, (RemotePodMessage)message.value());
					}	
				}
				else {
					Pair<Object, Integer> result = podRemoteFunction.handle(message);
					if (result!=null)
						internal_callback(this, message, result);
				}
			}
			
			@Override
			public void register() {
				podRemoteFunction = createFunction(this, getContext());
			}				
		};
	}
	
	protected void internal_callback(ActorRef host, ActorMessage<?> message, Pair<Object, Integer> result) {
		host.tell(result.a(), result.b(), message.source(), message.interaction(), message.protocol(), message.domain());
	}
	
	protected void internal_callback(RemotePodMessage remoteMessage, Pair<Object, Integer> result) {
		if (remoteMessage.remotePodMessageDTO().reply() && internal_server_callback!=null)
			internal_server_callback.accept(remoteMessage.replyAddress(), result.a(), result.b());
	}
	
	public abstract PodRemoteFunction createFunction(ActorRef host, PodContext context);
}
