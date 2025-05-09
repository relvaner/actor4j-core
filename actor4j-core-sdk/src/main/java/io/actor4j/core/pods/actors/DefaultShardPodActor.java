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

import io.actor4j.core.id.ActorId;

public abstract class DefaultShardPodActor extends PodActor {
	protected final ShardProxyPodActorFactory shardProxyPodActorFactory;
	protected final HandlerPodActorFactory handlerPodActorFactory;
	protected ActorId shardProxyPodActor;
	protected ActorId handlerPodActor;
	
	public DefaultShardPodActor(ShardProxyPodActorFactory shardProxyPodActorFactory, HandlerPodActorFactory handlerPodActorFactory) {
		super();
		this.shardProxyPodActorFactory = shardProxyPodActorFactory;
		this.handlerPodActorFactory = handlerPodActorFactory;
	}
	
	@Override
	public void preStart() {
		if (getContext().isShard())
			shardProxyPodActor = addChild(() -> shardProxyPodActorFactory.create(groupId, getContext()));
		handlerPodActor = addChild(() -> handlerPodActorFactory.create(groupId, getContext()));
		
		register();
	}
}
