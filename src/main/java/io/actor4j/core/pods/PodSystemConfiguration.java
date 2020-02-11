/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods;

import java.util.List;

public class PodSystemConfiguration {
	protected final List<String> primaryShardIds;
	protected final List<String> secondaryShardIds;
	protected final List<Integer> secondaryShardCounts;
	protected final int currentShardCount;
	protected final int currentReplicaCount;

	public PodSystemConfiguration(List<String> primaryShardIds, List<String> secondaryShardIds,
			List<Integer> secondaryShardCounts, int currentShardCount, int currentReplicaCount) {
		super();
		this.primaryShardIds = primaryShardIds;
		this.secondaryShardIds = secondaryShardIds;
		this.secondaryShardCounts = secondaryShardCounts;
		this.currentShardCount = currentShardCount;
		this.currentReplicaCount = currentReplicaCount;
	}

	public List<String> getPrimaryShardIds() {
		return primaryShardIds;
	}

	public List<String> getSecondaryShardIds() {
		return secondaryShardIds;
	}

	public List<Integer> getSecondaryShardCounts() {
		return secondaryShardCounts;
	}

	public int getCurrentShardCount() {
		return currentShardCount;
	}

	public int getCurrentReplicaCount() {
		return currentReplicaCount;
	}
}
