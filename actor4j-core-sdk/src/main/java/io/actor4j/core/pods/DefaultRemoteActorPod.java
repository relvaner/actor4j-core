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
package io.actor4j.core.pods;

import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.PodActorMessage;
import io.actor4j.core.pods.actors.DefaultPodActor;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.pods.actors.RemoteHandlerPodActor;
import io.actor4j.core.utils.ActorFactory;

public abstract class DefaultRemoteActorPod extends ActorPod {
	public DefaultRemoteActorPod() {
		super();
	}

	@Override
	public PodActor create() {
		return new DefaultPodActor((groupId, context) -> new RemoteHandlerPodActor(domain(), groupId, context) {
				@Override
				public void handle(ActorMessage<?> message, UUID interaction) {
					if (message instanceof PodActorMessage)
						sendViaAlias(PodActorMessage.create(message.value(), message.tag(), self(), null, interaction, ((PodActorMessage<?, ?, ?>) message).user(), ((PodActorMessage<?, ?, ?>) message).params(), message.protocol(), message.domain()), getAbsoluteAlias(domain()));
					else
						sendViaAlias(PodActorMessage.create(message.value(), message.tag(), self(), null, interaction, null, null, message.protocol(), message.domain()), getAbsoluteAlias(domain()));
				}

				@Override
				public void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, UUID dest, UUID interaction) {
					tell(message.value(), message.tag(), dest, interaction, message.protocol(), domain());
				}

				@Override
				public void handle(RemotePodMessage remoteMessage, UUID interaction) {
					sendViaAlias(PodActorMessage.create(remoteMessage.remotePodMessageDTO().payload(), remoteMessage.remotePodMessageDTO().tag(), self(), null, interaction, remoteMessage.user(), remoteMessage.remotePodMessageDTO().params(), null, null), getAbsoluteAlias(domain()));
				}

				@Override
				public Object callback(ActorMessage<?> message, RemotePodMessage remoteMessage) {
					return message.value();
				}
			}) {
			@Override
			public void register() {
				addChild(factory(groupId, getContext()));
			}

			@Override
			public void receive(ActorMessage<?> message) {
				unhandled(message);
			}
		};
	}
	
	public abstract ActorFactory factory(UUID groupId, PodContext context);
}
