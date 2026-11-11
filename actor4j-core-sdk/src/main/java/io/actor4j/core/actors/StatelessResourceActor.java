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

import java.util.function.Consumer;

import io.actor4j.core.messages.ActorMessage;

/*
 * Stateless resource actor: the same instance may run on several threads at once and in any order (default
 * and classic runtime; loom runs it sequentially). Therefore no fields with state and no behaviour changes:
 * become, unbecome and await (based on become) throw UnsupportedOperationException.
 */
public abstract class StatelessResourceActor extends ResourceActor {
	public StatelessResourceActor() {
		this(null);
	}
	
	@SuppressWarnings("deprecation")
	public StatelessResourceActor(String name) {
		super(name, false, false);
	}
	
	@Override
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		throw new UnsupportedOperationException("become is not supported by a stateless resource actor");
	}
	
	@Override
	public void unbecome() {
		throw new UnsupportedOperationException("unbecome is not supported by a stateless resource actor");
	}
	
	@Override
	public void unbecomeAll() {
		throw new UnsupportedOperationException("unbecomeAll is not supported by a stateless resource actor");
	}
}
