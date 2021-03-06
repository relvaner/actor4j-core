/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package io.actor4j.core.internal;

import java.util.List;

import io.actor4j.core.ActorSystem;

public class DefaultActorSystemImpl extends ActorSystemImpl {
	public DefaultActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}

	public DefaultActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		actorThreadFactory  = (group, n, system) -> new DefaultUnboundedActorThread(group, n, system);
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		return executerService.actorThreadPool.getWorkerInnerQueueSizes();
	}
	
	public List<Integer> getWorkerOuterQueueSizes() {
		return executerService.actorThreadPool.getWorkerOuterQueueSizes();
	}
}
