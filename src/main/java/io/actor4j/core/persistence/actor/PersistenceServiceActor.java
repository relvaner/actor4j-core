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
package io.actor4j.core.persistence.actor;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.persistence.connectors.PersistenceAdapter;

public class PersistenceServiceActor extends Actor {
	protected PersistenceAdapter adapter;
	
	public static final int PERSIST_EVENTS = 100;
	public static final int PERSIST_STATE  = 101;
	public static final int RECOVER  	   = 102;
	
	public PersistenceServiceActor(String name, PersistenceAdapter adapter) {
		super(name);
		this.adapter = adapter;
	}

	@Override
	public void preStart() {
		adapter.preStart(self());
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		adapter.receive(message);
	}
}
