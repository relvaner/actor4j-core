/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.extended.utils;

import java.util.Queue;

import io.actor4j.core.messages.ActorMessage;

public abstract class AbstractAntiFloodingHandler implements AntiFloodingHandler {
	protected final Queue<ActorMessage<?>> exposedQueue;
	protected final Queue<ActorMessage<?>> fallbackQueue;
	
	protected final int defaultQueueMaxSize;
	
	public AbstractAntiFloodingHandler(Queue<ActorMessage<?>> exposedQueue, Queue<ActorMessage<?>> fallbackQueue, int defaultQueueMaxSize) {
		super();
		
		this.exposedQueue = exposedQueue;
		this.fallbackQueue = fallbackQueue;
		this.defaultQueueMaxSize = defaultQueueMaxSize;
	}
	
	@Override
	public void handleQueue(ActorMessage<?> message, boolean overloadDetected) {
		// Layer One: fast path without lock, offer can still fail (queue full)
		if (overloadDetected || !exposedQueue.offer(message))
			handleOverload(message);
	}
	
	protected abstract void handleOverload(ActorMessage<?> message);
}
