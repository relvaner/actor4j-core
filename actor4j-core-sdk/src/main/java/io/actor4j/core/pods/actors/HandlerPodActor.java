/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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

import java.util.UUID;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.utils.PodActorMessageProxyHandler;
import io.actor4j.core.pods.utils.PodStatus;

public abstract class HandlerPodActor extends PodChildActor {
	protected final String alias;
	protected final PodActorMessageProxyHandler proxy;

	public HandlerPodActor(String alias, UUID groupId, PodContext context) {
		super(groupId, context);
		this.alias = alias;

		this.proxy = new PodActorMessageProxyHandler(this, groupId) {
			@Override
			public void handle(ActorMessage<?> message, UUID interaction) {
				HandlerPodActor.this.handle(message, interaction);
			}

			@Override
			public void unhandled(ActorMessage<?> message) {
//				HandlerPodActor.this.send(message.shallowCopy(PodStatus.LOOP_DETECTED, message.source()));
			}
			
			@Override
			public void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, ActorId dest, UUID interaction) {
				HandlerPodActor.this.callback(message, originalMessage, dest, interaction);
			}
		};
	}
	
	@Override
	public void preStart() {
		if (context.isShard())
			setAlias(alias+context.shardId(), false);
		else
			setAlias(alias, false);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		proxy.apply(message);
	}
	
	public abstract void handle(ActorMessage<?> message, UUID interaction);
	public abstract void callback(ActorMessage<?> message, ActorMessage<?> originalMessage, ActorId dest, UUID interaction);
}
