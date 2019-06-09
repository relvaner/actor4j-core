/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package io.actor4j.core.service.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.immutable.ImmutableObject;
import io.actor4j.core.messages.ActorMessage;

public class ServiceDiscoveryActor extends Actor {
	protected Map<UUID, Service> services;
	protected Map<String, Set<UUID>> topicsMap;
	
	protected String alias;
	
	public static final int PUBLISH_SERVICE   = 400;
	public static final int UNPUBLISH_SERVICE = 401;
	public static final int LOOKUP_SERVICES   = 402;
	public static final int LOOKUP_SERVICE    = 403;
	
	public ServiceDiscoveryActor(String alias) {
		this.alias = alias;
		
		services = new HashMap<>();
		topicsMap = new HashMap<>();
	}
	
	@Override
	public void preStart() {
		// registering alias
		getSystem().setAlias(self(), alias);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null) {
			if (message.tag==PUBLISH_SERVICE && message.value instanceof Service) {
				Service service = (Service)message.value;
				services.put(service.id, service);
				if (service.topics!=null) {
					for (String topic : service.topics) {
						Set<UUID> ids = topicsMap.get(topic);
						if (ids==null) {
							ids = new HashSet<>();
							topicsMap.put(topic, ids);
						}
						ids.add(service.id);
					}
				}
			}
			else if (message.tag==UNPUBLISH_SERVICE && message.value instanceof UUID) {
				Service service = services.get(message.value);
				if (service.topics!=null) {
					for (String topic : service.topics) {
						Set<UUID> ids = topicsMap.get(topic);
						if (ids!=null)
							ids.remove(service.id);
					}
				}
				services.remove(service.id);
			}
			else if (message.tag==LOOKUP_SERVICES && message.value instanceof String) {
				List<Service> result = new LinkedList<>();
				
				Set<UUID> ids = topicsMap.get(message.value);
				if (ids!=null) {
					Iterator<UUID> iterator = ids.iterator();
					while (iterator.hasNext())
						result.add(services.get(iterator.next()));
					
				}
				
				tell(new ImmutableList<>(result), LOOKUP_SERVICES, message.source);
			}
			else if (message.tag==LOOKUP_SERVICE && message.value instanceof String) {
				Service result = null;
				
				Set<UUID> ids = topicsMap.get(message.value);
				if (ids!=null) {
					Iterator<UUID> iterator = ids.iterator();
					if (iterator.hasNext())
						result = services.get(iterator.next());
				}
			
				tell(new ImmutableObject<>(result), LOOKUP_SERVICE, message.source);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
