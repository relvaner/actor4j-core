/*
 * Copyright (c) 2015-2019, David A. Bauer. All rights reserved.
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
package actor4j.core;

public class XAntiFloodingTimer {
	protected int count;
	protected int maxCount;
	protected long withinTimeRange;
	
	protected long startTime;
	protected long stopTime;
	
	protected boolean active;
	
	public XAntiFloodingTimer(int maxCount, long withinTimeRange) {
		this.maxCount = maxCount;
		this.withinTimeRange = withinTimeRange;
	}
	
	protected boolean isInTimeRange() {
		boolean result = true;
		
		if (!active) {
			active = true;
			count = 1;
			startTime = System.currentTimeMillis();
			stopTime  = startTime;
		}
		else
			count++;
		
		stopTime  = System.currentTimeMillis();
		result = ((stopTime-startTime) <= withinTimeRange) && (maxCount>0 ? count<maxCount : true);
		
		return result;
	}
	
	protected void inactive() {
		active = false;
	}
}
