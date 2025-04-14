/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
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

import io.actor4j.core.runtime.utils.ProcessingTimeStatistics;

public interface ActorExecutionUnitPool<U extends ActorExecutionUnit> {
	public void shutdown(Runnable onTermination, boolean await);
	
	public List<U> getExecutionUnitList();
	public ActorExecutionUnitPoolHandler<U> getExecutionUnitPoolHandler();
	
	public List<Boolean> getExecutionUnitLoads();
	public long getCount();
	public List<Long> getCounts();
	
	public default List<ProcessingTimeStatistics> getProcessingTimeStatistics() {
		List<ProcessingTimeStatistics> list = new ArrayList<>();
		for (U u : getExecutionUnitList())
			list.add(u.getProcessingTimeStatistics());
		return list;
	}
	
	public default List<Double> getMeanProcessingTime() {
		List<Double> list = new ArrayList<>();
		for (U u : getExecutionUnitList())
			list.add(u.getMeanProcessingTime());
		return list;
	}
	
	public default List<Double> getMedianProcessingTime() {
		List<Double> list = new ArrayList<>();
		for (U u : getExecutionUnitList())
			list.add(u.getMedianProcessingTime());
		return list;
	}
}
