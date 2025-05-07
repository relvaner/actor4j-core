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
package io.actor4j.core.runtime.classic.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Collectors;

import io.actor4j.core.runtime.classic.ActorRunnableMetrics;

public final class ClassicForkJoinWorkerThread extends ForkJoinWorkerThread {
	private static final ConcurrentHashMap<Long, ActorRunnableMetrics> metricsMap = new ConcurrentHashMap<>();
	
	protected ClassicForkJoinWorkerThread(ForkJoinPool pool) {
		super(pool);
		
		metricsMap.put(threadId(), new ActorRunnableMetrics(threadId()));
	}
	
	public static ActorRunnableMetrics getMetrics() {
		return metricsMap.get(Thread.currentThread().threadId());
	}
	
	public static List<ActorRunnableMetrics> getAllMetrics() {
		return metricsMap.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
	}
}
