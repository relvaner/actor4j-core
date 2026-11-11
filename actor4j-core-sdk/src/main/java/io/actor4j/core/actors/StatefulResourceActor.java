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
package io.actor4j.core.actors;

/*
 * Stateful resource actor: messages are processed one after another (like an actor), may keep state and use
 * become. bulk: all queued messages are passed at once as an ImmutableList (one receive per batch).
 */
public abstract class StatefulResourceActor extends ResourceActor {
	public StatefulResourceActor() {
		this(null, false);
	}
	
	public StatefulResourceActor(String name) {
		this(name, false);
	}
	
	@SuppressWarnings("deprecation")
	public StatefulResourceActor(String name, boolean bulk) {
		super(name, true, bulk);
	}
}
