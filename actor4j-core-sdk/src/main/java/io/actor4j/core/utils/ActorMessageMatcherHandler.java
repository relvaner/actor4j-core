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
package io.actor4j.core.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public class ActorMessageMatcherHandler {
	protected final Map<UUID, Pair<ActorMessage<?>, ActorMessageMatcher>> handlerMap;

	public ActorMessageMatcherHandler() {
		super();
		
		handlerMap = new LinkedHashMap<>();
	}
	
	public void clear() {
		handlerMap.clear();
	}
	
	public void define(UUID interaction, ActorMessage<?> message, ActorMessageMatcher matcher) {
		handlerMap.put(interaction, Pair.of(message, matcher));
	}

	public boolean match(ActorMessage<?> message) {
		boolean result = false;
		
		Pair<ActorMessage<?>, ActorMessageMatcher> pair = handlerMap.get(message.interaction());
		if (pair!=null && pair.b()!=null) {
			result = pair.b().apply(message);
			if (result)
				handlerMap.remove(message.interaction());
		}
		
		return result;
	}
	
	public ActorMessage<?> origin(UUID interaction) {
		Pair<ActorMessage<?>, ActorMessageMatcher> pair = handlerMap.get(interaction);
		
		return pair!=null && pair.a()!=null ? pair.a() : null;
	}
	
	public Optional<Integer> tag(UUID interaction) {
		ActorMessage<?> message = origin(interaction);

		return message!=null ? Optional.of(message.tag()) : Optional.empty();
	}
	
	public Optional<ActorId> source(UUID interaction) {
		ActorMessage<?> message = origin(interaction);

		return message!=null ? Optional.of(message.source()) : Optional.empty();
	}
	
	public Optional<ActorId> dest(UUID interaction) {
		ActorMessage<?> message = origin(interaction);

		return message!=null ? Optional.of(message.dest()) : Optional.empty();
	}
	
	public Optional<String> protocol(UUID interaction) {
		ActorMessage<?> message = origin(interaction);

		return message!=null ? Optional.of(message.protocol()) : Optional.empty();
	}
	
	public Optional<String> domain(UUID interaction) {
		ActorMessage<?> message = origin(interaction);

		return message!=null ? Optional.of(message.domain()) : Optional.empty();
	}
}
