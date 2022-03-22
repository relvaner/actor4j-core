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
package io.actor4j.core.internal.pods;

import java.util.List;

public final class PodSystemConfiguration {
	private final List<String> primaryShardIds; 
	private final List<String> secondaryShardIds;
	private final List<Integer> secondaryShardCounts; 
	private final int currentShardCount;
	private final int currentReplicaCount;
	
	public PodSystemConfiguration(List<String> primaryShardIds, List<String> secondaryShardIds,
			List<Integer> secondaryShardCounts, int currentShardCount, int currentReplicaCount) {
		super();
		this.primaryShardIds = primaryShardIds;
		this.secondaryShardIds = secondaryShardIds;
		this.secondaryShardCounts = secondaryShardCounts;
		this.currentShardCount = currentShardCount;
		this.currentReplicaCount = currentReplicaCount;
	}
	
	public List<String> primaryShardIds() {
		return primaryShardIds;
	}
	
	public List<String> secondaryShardIds() {
		return secondaryShardIds;
	}
	
	public List<Integer> secondaryShardCounts() {
		return secondaryShardCounts;
	}
	
	public int currentShardCount() {
		return currentShardCount;
	}
	
	public int currentReplicaCount() {
		return  currentReplicaCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentReplicaCount;
		result = prime * result + currentShardCount;
		result = prime * result + ((primaryShardIds == null) ? 0 : primaryShardIds.hashCode());
		result = prime * result + ((secondaryShardCounts == null) ? 0 : secondaryShardCounts.hashCode());
		result = prime * result + ((secondaryShardIds == null) ? 0 : secondaryShardIds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PodSystemConfiguration other = (PodSystemConfiguration) obj;
		if (currentReplicaCount != other.currentReplicaCount)
			return false;
		if (currentShardCount != other.currentShardCount)
			return false;
		if (primaryShardIds == null) {
			if (other.primaryShardIds != null)
				return false;
		} else if (!primaryShardIds.equals(other.primaryShardIds))
			return false;
		if (secondaryShardCounts == null) {
			if (other.secondaryShardCounts != null)
				return false;
		} else if (!secondaryShardCounts.equals(other.secondaryShardCounts))
			return false;
		if (secondaryShardIds == null) {
			if (other.secondaryShardIds != null)
				return false;
		} else if (!secondaryShardIds.equals(other.secondaryShardIds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PodSystemConfiguration [primaryShardIds=" + primaryShardIds + ", secondaryShardIds=" + secondaryShardIds
				+ ", secondaryShardCounts=" + secondaryShardCounts + ", currentShardCount=" + currentShardCount
				+ ", currentReplicaCount=" + currentReplicaCount + "]";
	}
}
