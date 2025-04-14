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

public abstract class AbstractActorExecutionUnitPool<U extends ActorExecutionUnit> implements ActorExecutionUnitPool<U>{
	protected final InternalActorRuntimeSystem system;
	
	protected final List<U> executionUnitList;
	protected final ActorExecutionUnitPoolHandler<U> executionUnitPoolHandler;

	public AbstractActorExecutionUnitPool(InternalActorRuntimeSystem system, ActorExecutionUnitPoolHandler<U> executionUnitPoolHandler) {
		this.system = system;
		this.executionUnitPoolHandler = executionUnitPoolHandler;
		
		executionUnitList = new ArrayList<>();
	}
	
	public abstract void shutdown(Runnable onTermination, boolean await);
	
	@Override
	public List<U> getExecutionUnitList() {
		return executionUnitList;
	}

	@Override
	public ActorExecutionUnitPoolHandler<U> getExecutionUnitPoolHandler() {
		return executionUnitPoolHandler;
	}
	
	@Override
	public List<Boolean> getExecutionUnitLoads() {
		List<Boolean> list = new ArrayList<>();
		for (U u : executionUnitList)
			list.add(u.getLoad().get());
		return list;
	}
	
	@Override
	public long getCount() {
		long sum = 0;
		for (U u : executionUnitList)
			sum += u.getCount();
		
		return sum;
	}
	
	@Override
	public List<Long> getCounts() {
		List<Long> list = new ArrayList<>();
		for (U u : executionUnitList)
			list.add(u.getCount());
		return list;
	}
}
