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
package io.actor4j.core.runtime;

import java.util.List;

import io.actor4j.core.ActorSystemFactory;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;

public class DefaultActorSystemImpl extends ActorSystemImpl implements DefaultInternalActorRuntimeSystem {
	protected /*quasi final*/ ActorThreadFactory actorThreadFactory;
	
	public DefaultActorSystemImpl() {
		this(null);
	}

	public DefaultActorSystemImpl(ActorSystemConfig config) {
		super(config);
		
		messageDispatcher = new DefaultActorMessageDispatcher(this);
		actorThreadFactory  = (group, n, system) -> new DefaultUnboundedActorThread(group, n, system); // TODO -> ActorThreadPool, ActorExecutorService
	}

	@Override
	public ActorSystemFactory factory() {
		return (config) -> new DefaultActorSystemImpl(config);
	}
	
	@Override
	protected InternalActorCell createResourceActorCell(Actor actor) {
		return new ResourceActorCell(this, actor);
	}
	
	@Override
	protected InternalActorCell createActorCell(Actor actor) {
		return new BaseActorCell(this, actor);
	}
	
	@Override
	protected InternalActorCell createPodActorCell(Actor actor) {
		return new PodActorCell(this, actor);
	}
	
	@Override
	protected ActorExecutorService createActorExecutorService() {
		return new DefaultActorExecutorService(this);
	}
	
	@Override
	public ActorThreadFactory getActorThreadFactory() {
		return actorThreadFactory;
	}

	public void setActorThreadFactory(ActorThreadFactory actorThreadFactory) {
		this.actorThreadFactory = actorThreadFactory;
	}
	
	public List<Integer> getWorkerInnerQueueSizes() {
		return ((DefaultInternalActorExecutorService)executorService).getThreadPool().getWorkerInnerQueueSizes();
	}

	public List<Integer> getWorkerOuterQueueSizes() {
		return ((DefaultInternalActorExecutorService)executorService).getThreadPool().getWorkerOuterQueueSizes();
	}
}
