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
package io.actor4j.core.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import io.actor4j.core.messages.ActorMessage;

public class ActorCacheHandler<K, V> {
	protected Map<UUID, Consumer<Pair<K, V>>> handlerMap;
	
	protected Function<ActorMessage<?>, Pair<K, V>> function;
	
	public ActorCacheHandler(Function<ActorMessage<?>, Pair<K, V>> function) {
		super();
		this.function = function;
		
		handlerMap = new LinkedHashMap<>();
	}
	
	public void define(UUID interaction, Consumer<Pair<K, V>> action) {
		handlerMap.put(interaction, action);
	}

	public boolean match(ActorMessage<?> message) {
		boolean result = false;
		
		Pair<K, V> pair = function.apply(message);
		if (pair!=null) {
			Consumer<Pair<K, V>> handler = handlerMap.get(message.interaction());
			if (handler!=null) {
				handler.accept(pair);
				handlerMap.remove(message.interaction());
				result = true;
			}
		}
		
		return result;
	}
}
