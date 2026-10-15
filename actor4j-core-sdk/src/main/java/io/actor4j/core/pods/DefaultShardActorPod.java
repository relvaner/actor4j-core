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
package io.actor4j.core.pods;

import java.util.UUID;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.messages.PodActorMessage;
import io.actor4j.core.pods.actors.DefaultShardPodActor;
import io.actor4j.core.pods.actors.HandlerPodActor;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.pods.actors.ShardProxyPodActor;
import io.actor4j.core.utils.ActorFactory;

public abstract class DefaultShardActorPod extends ActorPod {
	public DefaultShardActorPod() {
		super();
	}

	@Override
	public PodActor create() {
		return new DefaultShardPodActor(
				(groupId, context) -> new ShardProxyPodActor(domain(), groupId, context) {
					@Override
					public String shardId(ActorMessage<?> message, int totalShardCount) {
						return DefaultShardActorPod.this.shardId(message, totalShardCount);
					}
				}, 
				(groupId, context) -> new HandlerPodActor(domain(), groupId, context) {
					@Override
					public void handle(ActorMessage<?> message, UUID interaction) {
						if (message instanceof PodActorMessage)
							sendViaAlias(PodActorMessage.create(message.value(), message.tag(), self(), null, interaction, ((PodActorMessage<?, ?, ?>) message).user(), ((PodActorMessage<?, ?, ?>) message).params(), message.protocol(), message.domain()), getAbsoluteAlias(domain()));
						else
							sendViaAlias(PodActorMessage.create(message.value(), message.tag(), self(), null, interaction, null, null, message.protocol(), message.domain()), getAbsoluteAlias(domain()));
					}
	
					@Override
					public void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, ActorId dest, UUID interaction) {
						tell(message.value(), message.tag(), dest, interaction, message.protocol(), domain());
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
	
	public abstract String shardId(ActorMessage<?> message, int totalShardCount);
}
