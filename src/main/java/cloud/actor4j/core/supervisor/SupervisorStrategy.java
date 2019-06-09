/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package cloud.actor4j.core.supervisor;

public abstract class SupervisorStrategy {
	protected int retries;
	protected int maxRetries;
	protected long withinTimeRange;
	
	protected long startTime;
	protected long stopTime;
	
	public SupervisorStrategy(int maxRetries, long withinTimeRange) {
		this.maxRetries = maxRetries;
		this.withinTimeRange = withinTimeRange;
	}
	
	protected boolean isInTimeRange() {
		return (stopTime-startTime) <= withinTimeRange;
	}
	
	protected void reset() {
		retries   = 0;
		
		startTime = System.currentTimeMillis();
		stopTime  = startTime;
	}
	
	public SupervisorStrategyDirective handle(Exception e) {
		if (startTime==0)
			reset();
		else
			stopTime  = System.currentTimeMillis();
		
		if (!isInTimeRange())
			if (maxRetries>0 && retries>=maxRetries)
				return SupervisorStrategyDirective.STOP;
			else
				reset();
		
		retries++;
		
		return apply(e);
	}
	
	public abstract SupervisorStrategyDirective apply(Exception e);
}
