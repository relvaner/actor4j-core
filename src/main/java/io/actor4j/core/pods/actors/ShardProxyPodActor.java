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

import java.util.Map;
import java.util.UUID;

import io.actor4j.core.function.TriConsumer;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.PodReplicationTuple;
import io.actor4j.core.pods.Shard;

// One-Way
public abstract class ShardProxyPodActor extends PodChildActor implements Shard {
	public static TriConsumer<ActorMessage<?>, String, String> internal_server_proxy;
	
	protected String alias;
	protected int shardCount;
	
	public ShardProxyPodActor(String alias, UUID groupId, PodContext context) {
		super(groupId, context);
		
		this.alias = alias;
	}
	
	@Override
	public void preStart() {
		setAlias(alias, false);
		
		Map<String, PodReplicationTuple> podReplicationMap = getSystem().underlyingImpl().getPodReplicationController().getPodReplicationMap();
		shardCount = podReplicationMap.get(context.getDomain()).getPodSystemConfiguration().getCurrentShardCount(); 
	}

	@Override
	public void receive(ActorMessage<?> message) {
		if (internal_server_proxy!=null)
			internal_server_proxy.accept(message, context.getDomain(), shardId(message, shardCount));
		else
			forward(message, getShardAlias(shardId(message, shardCount)));
	}
	
	public String getShardAlias(String shardId) {
		return alias+shardId;
	}
}
