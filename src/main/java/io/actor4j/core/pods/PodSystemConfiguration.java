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
