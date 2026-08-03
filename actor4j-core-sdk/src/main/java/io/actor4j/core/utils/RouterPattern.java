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
package io.actor4j.core.utils;

import java.util.HashMap;
import java.util.Map;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public class RouterPattern<T> {
	protected final ActorRef actorRef;
	
	protected final Map<T, ActorId> routes;

	public RouterPattern(ActorRef actorRef) {
		super();
		
		this.actorRef = actorRef;
		
		routes = new HashMap<>();
	}
	
	public RouterPattern(ActorRef actorRef, Map<T, ActorId> routes) {
		this(actorRef);
		
		routes.putAll(routes);
	}
	
	public Map<T, ActorId> getRoutes() {
		return routes;
	}

	public void put(T routeId, ActorId id) {
		routes.put(routeId, id);
	}
	
	public void putAll(Map<T, ActorId> routes) {
		routes.putAll(routes);
	}
	
	public void remove(T routeId) {
		routes.remove(routeId);
	}
	
	public boolean containsKey(T routeId) {
		return routes.containsKey(routeId);
	}
	
	public boolean containsValue(ActorId id) {
		return routes.containsValue(id);
	}
	
	public int count() {
		return routes.size();
	}
	
	public ActorId resolve(T routeId) {
		return routes.get(routeId);
	}
	
	public boolean route(ActorMessage<?> message, T routeId) {
		ActorId dest = routes.get(routeId);
		boolean result = (dest!=null);
		
		if (result)
			actorRef.send(message, dest);
		
		return result;
	}
}
