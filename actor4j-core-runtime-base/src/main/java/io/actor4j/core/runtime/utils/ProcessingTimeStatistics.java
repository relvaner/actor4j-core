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
package io.actor4j.core.runtime.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public record ProcessingTimeStatistics(long mean, long median, long min, long max) {
	public static ProcessingTimeStatistics of(Queue<Long> values) {
		long median = medianProcessingTime(values);
		
		long sum = 0, min = 0, max = 0;
		int count = 0;
		for (Long value=null; (value=values.poll())!=null; count++) {
			sum += value;
			min = Math.min(min, value);
			max = Math.max(max, value);
		}
		
		return new ProcessingTimeStatistics(count>0 ? sum/count : 0, median, min , max);
	}
	
	public static long meanProcessingTime(Queue<Long> values) {
		long sum = 0;
		int count = 0;
		for (Long value=null; (value=values.poll())!=null; count++) 
			sum += value;
		
		return count>0 ? sum/count : 0;
	}
	
	public static long medianProcessingTime(Queue<Long> values) {
		if (values.size()>0) {
			List<Long> sorted = new ArrayList<>(values);
			Collections.sort(sorted);
			int size = sorted.size();
			if (size % 2 == 0) 
				return (sorted.get(size/2 -1) + sorted.get(size/2)) / 2;
			else
				return sorted.get(size/2);
		}
		else
			return 0;
	}
}
