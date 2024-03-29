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
package io.actor4j.core.runtime.persistence.actor;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.persistence.drivers.PersistenceImpl;

public class PersistenceServiceActor extends Actor {
	protected final PersistenceImpl impl;
	
	public static final int PERSIST_EVENTS = 100;
	public static final int PERSIST_STATE  = 101;
	public static final int RECOVER  	   = 102;
	
	public PersistenceServiceActor(String name, PersistenceImpl impl) {
		super(name);
		this.impl = impl;
	}

	@Override
	public void preStart() {
		impl.preStart(self());
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		impl.receive(message);
	}
}
