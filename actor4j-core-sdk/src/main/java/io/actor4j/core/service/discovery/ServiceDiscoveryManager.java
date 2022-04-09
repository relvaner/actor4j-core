/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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

import static io.actor4j.core.service.discovery.ServiceDiscoveryActor.*;

import java.util.Optional;
import java.util.UUID;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.immutable.ImmutableObject;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;

public class ServiceDiscoveryManager {
	protected ActorRef actorRef;
	protected String serviceDiscoveryAlias;

	public ServiceDiscoveryManager(ActorRef actorRef, String serviceDiscoveryAlias) {
		super();
		
		this.actorRef = actorRef;
		this.serviceDiscoveryAlias = serviceDiscoveryAlias;
	}
	
	public ActorFactory create() {
		return () -> new ServiceDiscoveryActor(serviceDiscoveryAlias);
	}
	
	public static ActorFactory create(String serviceDiscoveryAlias) {
		return () -> new ServiceDiscoveryActor(serviceDiscoveryAlias);
	}
	
	public void publish(Service service) {
		actorRef.tell(service, PUBLISH_SERVICE, serviceDiscoveryAlias);
	}
	
	public void unpublish(UUID service) {
		actorRef.tell(service, UNPUBLISH_SERVICE, serviceDiscoveryAlias);
	}
	
	public void lookupFirst(String topic) {
		actorRef.tell(topic, LOOKUP_SERVICE, serviceDiscoveryAlias);
	}
	
	public void lookup(String topic) {
		actorRef.tell(topic, LOOKUP_SERVICES, serviceDiscoveryAlias);
	}
	
	@SuppressWarnings("unchecked")
	public Optional<ImmutableObject<Service>> lookupFirst(ActorMessage<?> message) {	
		if (message.tag()==LOOKUP_SERVICE && message.value()!=null && message.value() instanceof ImmutableObject) 
			return Optional.of(((ImmutableObject<Service>)message.value()));
		else
			return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	public Optional<ImmutableList<Service>> lookup(ActorMessage<?> message) {	
		if (message.tag()==LOOKUP_SERVICES && message.value()!=null && message.value() instanceof ImmutableList)
			return Optional.of(((ImmutableList<Service>)message.value()));
		else
			return Optional.empty();
	}
}
