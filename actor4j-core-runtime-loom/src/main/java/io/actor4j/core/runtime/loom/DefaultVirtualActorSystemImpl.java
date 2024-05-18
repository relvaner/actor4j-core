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
package io.actor4j.core.runtime.loom;

import java.util.UUID;

import io.actor4j.core.ActorSystemFactory;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.runtime.ActorExecutorService;
import io.actor4j.core.runtime.ActorSystemImpl;
import io.actor4j.core.runtime.BaseActorCell;
import io.actor4j.core.runtime.InternalActorCell;
import io.actor4j.core.runtime.PodActorCell;

public class DefaultVirtualActorSystemImpl extends ActorSystemImpl {
	public DefaultVirtualActorSystemImpl() {
		this(null);
	}

	public DefaultVirtualActorSystemImpl(ActorSystemConfig config) {
		super(config);
		
		messageDispatcher = new DefaultVirtualActorMessageDispatcher(this);
	}
	
	@Override
	public ActorSystemFactory factory() {
		return (config) -> new DefaultVirtualActorSystemImpl(config);
	}
	
	@Override
	protected InternalActorCell createResourceActorCell(Actor actor) {
		return new BaseActorCell(this, actor);
	}
	
	@Override
	protected InternalActorCell createActorCell(Actor actor) {
		return new BaseActorCell(this, actor);
	}
	
	@Override
	protected InternalActorCell createActorCell(Actor actor, UUID id) {
		return new BaseActorCell(this, actor, id);
	}
	
	@Override
	protected InternalActorCell createPodActorCell(Actor actor) {
		return new PodActorCell(this, actor);
	}
	
	@Override
	protected ActorExecutorService createActorExecutorService() {
		return new DefaultVirtualActorExecutorService(this);
	}
}
