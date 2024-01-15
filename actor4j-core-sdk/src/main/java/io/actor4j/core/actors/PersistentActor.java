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

import static io.actor4j.core.runtime.protocols.ActorProtocolTag.INTERNAL_RECOVER;

import java.util.function.Consumer;

import io.actor4j.core.json.JsonObject;

public abstract class PersistentActor<S, E> extends Actor implements PersistenceId {
	public static final int RECOVER = INTERNAL_RECOVER;
	
	public PersistentActor() {
		super();
	}
	
	public PersistentActor(String name) {
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	public void persist(Consumer<E> onSuccess, Consumer<Exception> onFailure, E... events) {	
		cell.persist(onSuccess, onFailure, events);
	}
	
	public void saveSnapshot(Consumer<S> onSuccess, Consumer<Exception> onFailure, S state) {
		cell.saveSnapshot(onSuccess, onFailure, state);
	}
	
	public void recover(JsonObject value) {
		// empty
	}
}
