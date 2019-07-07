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

import static io.actor4j.core.actors.ActorWithCache.SUBSCRIBE_SECONDARY;

import java.util.UUID;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;

public abstract class SecondaryActor extends ActorWithDistributedGroup {
	protected /*final*/ UUID primary;
	
	public SecondaryActor(ActorGroup group, UUID primary) {
		this(null, group, primary);
	}

	public SecondaryActor(String name, ActorGroup group, UUID primary) {
		super(name, group);
		this.primary = primary;
	}
	
	public void publish(ActorMessage<?> message) {
		send(message, primary);
	}
	
	public <T> void publish(T value, int tag) {
		tell(value, tag, primary);
	}
	
	public void subscribeAsSecondary() {
		tell(null, SUBSCRIBE_SECONDARY, primary);
	}
}
