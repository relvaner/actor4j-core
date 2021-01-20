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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.utils.Pair;

public class ExampleReplicationWithRemoteFunctionImpl {
	protected ActorRef host;
	protected PodContext context;
	
	protected Map<UUID, BiFunction<Object, Integer, Pair<Object, Integer>>> handlerMap;
	
	public ExampleReplicationWithRemoteFunctionImpl(ActorRef host, PodContext context) {
		super();
		this.host = host;
		this.context = context;
		
		handlerMap = new HashMap<>();
	}

	public Pair<Object, Integer> handle(ActorMessage<?> message) {
		Pair<Object, Integer> result = null;
		
		BiFunction<Object, Integer, Pair<Object, Integer>> handler = handlerMap.get(message.interaction);
		if (handler!=null && message.value!=null && message.value instanceof String) {
			result = handler.apply(message.value, message.tag);
			handlerMap.remove(message.interaction);
		}
		else {
			handlerMap.put(message.interaction, (value, tag) -> {
				return Pair.of(value, tag);
			});
			host.tell(message.value, message.tag, "ExampleReplicationWithFunctionPod", message.interaction, null, context.getDomain());
		}
			
		return result;
	}
}
