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
package io.actor4j.core.runtime.loom;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class VirtualActorRunnableMetrics {
	protected final AtomicLong counter;
//	protected final AtomicBoolean load;
	
	protected final AtomicInteger processingTimeSampleCount;
	protected final Queue<Long> processingTimeSamples;

	protected final AtomicInteger cellsProcessingTimeSampleCount;
	
	static {
		
	}

	public VirtualActorRunnableMetrics() {
		super();

//		load = new AtomicBoolean(false);
		counter = new AtomicLong(0);
		
		processingTimeSampleCount = new AtomicInteger(0);
		processingTimeSamples = new ConcurrentLinkedQueue<>();
		
		cellsProcessingTimeSampleCount = new AtomicInteger(0);
	}
}
