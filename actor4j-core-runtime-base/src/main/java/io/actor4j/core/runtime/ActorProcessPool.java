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

public abstract class ActorProcessPool<P extends ActorProcess> {
	protected final InternalActorRuntimeSystem system;
	
	protected final List<P> actorProcessList;
	protected final ActorProcessPoolHandler<P> actorProcessPoolHandler;

	public ActorProcessPool(InternalActorRuntimeSystem system, ActorProcessPoolHandler<P> actorProcessPoolHandler) {
		this.system = system;
		this.actorProcessPoolHandler = actorProcessPoolHandler;
		
		actorProcessList = new ArrayList<>();
	}
	
	public abstract void shutdown(Runnable onTermination, boolean await);
	
	public List<P> getActorProcessList() {
		return actorProcessList;
	}

	public ActorProcessPoolHandler<P> getActorProcessPoolHandler() {
		return actorProcessPoolHandler;
	}
	
	public List<Boolean> getProcessLoads() {
		List<Boolean> list = new ArrayList<>();
		for (P p : actorProcessList)
			list.add(p.getLoad().get());
		return list;
	}
	
	public List<Long> getProcessingTimeStatistics() {
		List<Long> list = new ArrayList<>();
		for (P p : actorProcessList)
			list.add(p.getProcessingTimeStatistics());
		return list;
	}
	
	public long getCount() {
		long sum = 0;
		for (P p : actorProcessList)
			sum += p.getCount();
		
		return sum;
	}
	
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		for (P p : actorProcessList)
			list.add(p.getCount());
		return list;
	}
}
