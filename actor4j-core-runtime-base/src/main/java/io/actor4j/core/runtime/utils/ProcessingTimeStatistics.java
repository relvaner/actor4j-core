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

import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Queue;
import java.util.stream.Collectors;

public record ProcessingTimeStatistics(double mean, double median, double sd, long min, long max, long count) {
	public static ProcessingTimeStatistics of(Queue<Long> values) {
		return of(values, -1);
	}
	
	public static ProcessingTimeStatistics of(Queue<Long> values, double zScoreThreshold) {
		Queue<Long> copyOfValues = new LinkedList<>(values);
		
		long sum = 0, min = 0, max = 0, count = 0;
		for (Long value=null; (value=values.poll())!=null; count++) {
			sum += value;
			if (zScoreThreshold<0) {
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
		}
		
		double mean = 0, median = 0, sd = 0;
		if (count>0) {
			mean = (double)sum/count;
			sd = standardDeviationProcessingTime(copyOfValues, mean);
			if (zScoreThreshold<0)
				median = medianProcessingTime(copyOfValues);
			else {
				final double mean_ = mean;
				final double sd_ = sd;
				copyOfValues = copyOfValues.stream().filter(v -> Math.abs(calculateZScore(v, mean_, sd_)) <= zScoreThreshold)
					.collect(Collectors.toCollection(LinkedList::new));
				LongSummaryStatistics statistics = copyOfValues.stream().mapToLong(Long::longValue).summaryStatistics();
				mean = statistics.getAverage();
				median = medianProcessingTime(copyOfValues);
				sd = standardDeviationProcessingTime(copyOfValues, mean);
				min = statistics.getMin();
				max = statistics.getMax();
				count = statistics.getCount();	
			}
		}
		
		return new ProcessingTimeStatistics(mean, median, sd, min, max, count);
	}
	
	public static double meanProcessingTime(Queue<Long> values) {
		long sum = 0;
		int count = 0;
		for (Long value=null; (value=values.poll())!=null; count++) 
			sum += value;
		
		return count>0 ? (double)sum/count : 0;
	}
	
	public static double medianProcessingTime(Queue<Long> values) {
		if (values.size()>0) {
			List<Long> sortedList = values.stream().sorted().collect(Collectors.toList());

			int size = sortedList.size();
			if (size % 2 == 0) 
				return (sortedList.get(size/2 -1) + sortedList.get(size/2)) / 2d;
			else
				return sortedList.get(size/2);
		}
		else
			return 0;
	}
	
	public static double standardDeviationProcessingTime(Queue<Long> values, double mean) {
		double variance = values.stream().mapToDouble(v -> Math.pow(v-mean, 2)).sum()/values.size();
		
		return Math.sqrt(variance);
	}
	
	public static double calculateZScore(long value, double mean, double sd) {
		if (sd==0) 
			return 0;
		
		return (value-mean)/sd;
	}
	
	public String describe() {
		return describe(0);
	}
	
	public String describe(int precision) {
		String floatFormat = "%." + precision + "f";
		
		return String.format("mean="+floatFormat+", median="+floatFormat+", sd="+floatFormat+", min=%d, max=%d, count=%d", 
			mean, median, sd, min, max, count);
	}
}
