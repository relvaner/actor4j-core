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
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import io.actor4j.core.messages.ActorMessage;

public class ActorMessageHandler<T> {
	protected final Map<UUID, BiConsumer<T, ActorMessage<?>>> handlerMap;
	
	protected final Class<T> clazz;
	protected final Predicate<ActorMessage<?>> predicate;
	
	public ActorMessageHandler(Class<T> clazz, Predicate<ActorMessage<?>> predicate) {
		super();
		this.clazz = clazz;
		this.predicate = predicate;
		
		handlerMap = new LinkedHashMap<>();
	}
	
	public ActorMessageHandler(Class<T> clazz) {
		this(clazz, null);
	}
	
	public void clear() {
		handlerMap.clear();
	}
	
	public void define(UUID interaction, BiConsumer<T, ActorMessage<?>> action) {
		handlerMap.put(interaction, action);
	}

	@SuppressWarnings("unchecked")
	public boolean match(ActorMessage<?> message) {
		boolean result = true;
		
		BiConsumer<T, ActorMessage<?>> handler = handlerMap.get(message.interaction());
		if (handler!=null && message.value()!=null && message.value().getClass().equals(clazz)) {
			if (predicate!=null)
				result = predicate.test(message);
			if (result) {
				handler.accept((T)message.value(), message);
				handlerMap.remove(message.interaction());
			}
		}
		else
			result = false;
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public boolean matchOfNullable(ActorMessage<?> message) {
		boolean result = true;
		
		BiConsumer<T, ActorMessage<?>> handler = handlerMap.get(message.interaction());
		if (handler!=null) {
			if (message.value()!=null) {
				if (message.value().getClass().equals(clazz)) {
					if (predicate!=null)
						result = predicate.test(message);
					if (result) {
						handler.accept((T)message.value(), message);
						handlerMap.remove(message.interaction());
					}
				}
				else
					result = false;
			}
			else {
				if (predicate!=null)
					result = predicate.test(message);
				if (result) {
					handler.accept(null, message);
					handlerMap.remove(message.interaction());
				}
			}
		}
		else
			result = false;
		
		return result;
	}
}
