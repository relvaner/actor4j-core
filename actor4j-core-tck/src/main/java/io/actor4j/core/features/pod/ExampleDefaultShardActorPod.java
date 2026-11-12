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
package io.actor4j.core.features.pod;

import java.util.UUID;

import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.DefaultShardActorPod;
import io.actor4j.core.pods.PodContext;

public class ExampleDefaultShardActorPod extends DefaultShardActorPod {
	@Override
	public ActorFactory factory(UUID groupId, PodContext context) {
		return () -> new HelloActor(groupId, context) {
			@Override
			public void preStart() {
				setAlias(context.domain());
			}
		};
	}

	@Override
	public String shardId(ActorMessage<?> message, int totalShardCount) {
		return String.valueOf(Math.floorMod(String.valueOf(message.value()).hashCode(), totalShardCount));
	}

	@Override
	public String domain() {
		return "ExampleDefaultShardActorPod";
	}
}
