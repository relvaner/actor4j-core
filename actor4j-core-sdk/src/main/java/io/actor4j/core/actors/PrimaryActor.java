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
package io.actor4j.core.actors;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorFactory;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.core.utils.HubPattern;

public abstract class PrimaryActor extends ActorWithDistributedGroup {
	protected String alias;
	protected Function<UUID, ActorFactory> secondary;
	protected int instances;
	
	protected HubPattern hub;
	
	public PrimaryActor(ActorGroup group, String alias, Function<UUID, ActorFactory> secondary, int instances) {
		this(null, group, alias, secondary, instances);
	}

	public PrimaryActor(String name, ActorGroup group, String alias, Function<UUID, ActorFactory> secondary, int instances) {
		super(name, group);
		this.alias = alias;
		this.secondary = secondary;
		this.instances = instances;
	}
	
	@Override
	public void preStart() {
		List<UUID> ids = null;

		if (secondary!=null && instances>0)
			// creating secondary actors
			ids = addChild(secondary.apply(self()), instances);
		else
			ids = new LinkedList<>();

		ActorGroupSet secondaryGroup = new ActorGroupSet(ids);
		hub = new HubPattern(this, secondaryGroup);
		
		ids.add(self());
		// registering alias
		getSystem().setAlias(ids, alias);
	}

	public void publish(ActorMessage<?> message) {
		hub.broadcast(message);
	}
	
	public <T> void publish(T value, int tag) {
		hub.broadcast(value, tag);
	}
}
