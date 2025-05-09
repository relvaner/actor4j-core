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
package io.actor4j.core.runtime.embedded;

import java.util.HashMap;
import java.util.Map;

import io.actor4j.core.id.ActorId;

public class ActorEmbeddedRouter extends HashMap<ActorId, InternalEmbeddedActorCell> {
	protected static final long serialVersionUID = 1L;

	public ActorEmbeddedRouter() {
		super();
	}

	public ActorEmbeddedRouter(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ActorEmbeddedRouter(int initialCapacity) {
		super(initialCapacity);
	}

	public ActorEmbeddedRouter(Map<? extends ActorId, ? extends InternalEmbeddedActorCell> m) {
		super(m);
	}
}
