/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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
package io.actor4j.core.utils;

import static io.actor4j.core.utils.CircuitBreaker.CircuitBreakerState.*;

// @See: https://martinfowler.com/bliki/CircuitBreaker.html
public class CircuitBreaker {
	public enum CircuitBreakerState {
		CLOSED,
		OPEN,
		HALF_OPEN
	}
	
	protected CircuitBreakerState state;
	
	protected final int maxFailures;
	protected final long resetTimeout;
	
	protected int failureCount;
	protected long lastFailureTime;
	
	
	public CircuitBreaker(int maxFailures, long resetTimeout) {
		super();
		
		this.maxFailures = maxFailures;
		this.resetTimeout = resetTimeout;
		
		failureCount = 0;
		lastFailureTime = 0;
		
		state = CLOSED;
	}

	public CircuitBreakerState getState() {
		return state;
	}

	public boolean isCallable() {
		if (failureCount >= maxFailures) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastFailureTime >= resetTimeout)
				state = HALF_OPEN;
			else
				state = OPEN;
		}
		else
			state = CLOSED;

		return (state==CLOSED || state==HALF_OPEN);
	}
	
	public void success() {
		failureCount = 0;
		lastFailureTime = 0;
	}
    
	public void failure() {
		failureCount++;
		lastFailureTime = System.currentTimeMillis();
	}
}
