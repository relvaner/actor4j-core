/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.classic;

import io.actor4j.core.runtime.ActorExecutorServiceImpl;
import io.actor4j.core.runtime.ActorProcessPool;
import io.actor4j.core.runtime.InternalActorRuntimeSystem;

public class ClassicDefaultActorExecutorService extends ActorExecutorServiceImpl<ActorRunnable> implements ClassicInternalActorExecutorService {
	public ClassicDefaultActorExecutorService(InternalActorRuntimeSystem system) {
		super(system);
	}
	
	@Override
	public ActorProcessPool<ActorRunnable> createActorProcessPool() {
		return new ActorRunnablePool(system);
	}
	
	@Override
	public ActorRunnablePool getActorRunnablePool() {
		return (ActorRunnablePool)actorProcessPool;
	}
}
