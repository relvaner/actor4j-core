/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.service.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class ServiceDiscoveryActor extends Actor {
	protected Map<UUID, Service> services;
	protected Map<String, Set<UUID>> topicsMap;
	
	public static final int REGISTER_SERVICE   = 400;
	public static final int UNREGISTER_SERVICE = 401;
	public static final int GET_SERVICES       = 402;
	public static final int GET_SERVICE        = 403;
	
	public ServiceDiscoveryActor() {
		services = new HashMap<>();
		topicsMap = new HashMap<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null) {
			if (message.tag==REGISTER_SERVICE && message.value instanceof Service) {
				Service service = (Service)message.value;
				services.put(service.getId(), service);
				if (service.topics!=null) {
					for (String topic : service.topics) {
						Set<UUID> ids = topicsMap.get(topic);
						if (ids==null) {
							ids = new HashSet<>();
							topicsMap.put(topic, ids);
						}
						ids.add(service.getId());
					}
				}
			}
			else if (message.tag==UNREGISTER_SERVICE && message.value instanceof UUID) {
				Service service = services.get(message.value);
				if (service.topics!=null) {
					for (String topic : service.topics) {
						Set<UUID> ids = topicsMap.get(topic);
						if (ids!=null)
							ids.remove(service.getId());
					}
				}
				services.remove(service.getId());
			}
			else if (message.tag==GET_SERVICES && message.value instanceof String) {
				List<Service> result = new LinkedList<>();
				
				Set<UUID> ids = topicsMap.get(message.value);
				if (ids!=null) {
					Iterator<UUID> iterator = ids.iterator();
					while (iterator.hasNext())
						result.add(services.get(iterator.next()));
					
				}
				
				tell(result, GET_SERVICES, message.source);
			}
			else if (message.tag==GET_SERVICE && message.value instanceof String) {
				Service result = null;
				
				Set<UUID> ids = topicsMap.get(message.value);
				if (ids!=null) {
					Iterator<UUID> iterator = ids.iterator();
					if (iterator.hasNext())
						result = services.get(iterator.next());
				}
				
				tell(result, GET_SERVICE, message.source);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
