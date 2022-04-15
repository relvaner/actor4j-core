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
package io.actor4j.core.runtime;

import java.util.ArrayList;
import java.util.List;

public class DefaultActorExecuterService extends ActorExecuterServiceImpl implements InternalActorExecuterService {
	protected /*quasi final*/ ActorThreadPool actorThreadPool;
	
	public DefaultActorExecuterService(InternalActorRuntimeSystem system) {
		super(system);
	}
	
	public void createActorProcessPool() {
		actorThreadPool = new ActorThreadPool(system);
	}
	
	public void shutdownActorProcessPool(Runnable onTermination, boolean await) {
		actorThreadPool.shutdown(onTermination, await);
	}
	
	@Override
	public ActorThreadPool getActorThreadPool() {
		return actorThreadPool;
	}
	
	@Override
	public long getCount() {
		return actorThreadPool!=null ? actorThreadPool.getCount() : 0;
	}
	
	@Override
	public List<Long> getCounts() {
		return actorThreadPool!=null ? actorThreadPool.getCounts() : new ArrayList<>();
	}
}
