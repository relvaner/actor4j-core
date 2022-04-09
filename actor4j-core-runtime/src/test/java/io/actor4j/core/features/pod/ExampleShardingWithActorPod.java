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
package io.actor4j.core.features.pod;

import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.ActorPod;
import io.actor4j.core.pods.actors.DefaultShardPodActor;
import io.actor4j.core.pods.actors.HandlerPodActor;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.pods.actors.ShardProxyPodActor;

public class ExampleShardingWithActorPod extends ActorPod {
	@Override
	public PodActor create() {
		return new DefaultShardPodActor(
			(groupId, context) -> new ShardProxyPodActor(domain(), groupId, context) {
				@Override
				public String shardId(ActorMessage<?> message, int totalShardCount) {
					int hashCode = String.valueOf(message.value()).hashCode();
					return String.valueOf(hashCode%totalShardCount);
				}
			}, 
			(groupId, context) -> new HandlerPodActor(domain(), groupId, context) {
				@Override
				public void handle(ActorMessage<?> message, UUID interaction) {
					sendViaAlias(ActorMessage.create(message.value(), message.tag(), self(), null, interaction, "", ""), "hello"+groupId);
				}

				@Override
				public void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, UUID dest, UUID interaction) {
					tell(message.value(), message.tag(), dest, interaction);
				}
			}) {
			@Override
			public void register() {
				addChild(() -> new HelloActor(groupId, getContext()));
			}

			@Override
			public void receive(ActorMessage<?> message) {
				unhandled(message);
			}
		};
	}

	@Override
	public String domain() {
		return "ExampleShardingWithActorPod";
	}
}
