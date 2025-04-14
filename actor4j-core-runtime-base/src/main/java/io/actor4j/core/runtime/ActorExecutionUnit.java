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

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.actor4j.core.runtime.utils.ProcessingTimeStatistics;

public interface ActorExecutionUnit extends Runnable {
	public Object executionUnitId();
	
	public default long executionUnitIdAsLong() {
		return (long)executionUnitId();
	}
	
	public long getCount();
	public AtomicBoolean getLoad();
	
	public Queue<Long> getProcessingTimeSamples();
	public AtomicInteger getProcessingTimeSampleCount();
	
	public AtomicInteger getCellsProcessingTimeSampleCount();
	
	public default ProcessingTimeStatistics getProcessingTimeStatistics() {
		return getProcessingTimeStatistics(-1);
	}
	
	public default ProcessingTimeStatistics getProcessingTimeStatistics(double zScoreThreshold) {
		ProcessingTimeStatistics result = ProcessingTimeStatistics.of(getProcessingTimeSamples(), zScoreThreshold);
		getProcessingTimeSampleCount().set(0);
		
		return result;
	}
	
	public default double getMeanProcessingTime() {
		double result = ProcessingTimeStatistics.meanProcessingTime(getProcessingTimeSamples());
		getProcessingTimeSampleCount().set(0);
		
		return result;
	}
	
	public default double getMedianProcessingTime() {
		double result = ProcessingTimeStatistics.medianProcessingTime(getProcessingTimeSamples());
		getProcessingTimeSamples().clear();
		getProcessingTimeSampleCount().set(0);
		
		return result;
	}
}
