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
package io.actor4j.core.utils;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentActorGroupQueue extends ConcurrentLinkedQueue<UUID> implements ActorGroup {
	protected static final long serialVersionUID = 1L;
	
	protected final UUID id;

	public ConcurrentActorGroupQueue() {
		super();

		id = UUID.randomUUID();
	}

	public ConcurrentActorGroupQueue(Collection<? extends UUID> c) {
		super(c);

		id = UUID.randomUUID();
	}

	@Override
	public UUID getId() {
		return id;
	}
}
