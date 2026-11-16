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
package io.actor4j.core.pods;

public record PodDeployment(int shardCount, int minReplica, int maxReplica, String versionNumber) {
	
	public PodDeployment {
		validate(shardCount, minReplica, maxReplica);
	}
	
	// also used by PodConfiguration
	public static void validate(int shardCount, int minReplica, int maxReplica) {
		if (shardCount<1 || minReplica<1 || maxReplica<minReplica)
			throw new IllegalArgumentException(String.format("Invalid pod configuration (shardCount=%d, minReplica=%d, maxReplica=%d)", shardCount, minReplica, maxReplica));
	}
	
	// one shard, one replica
	public PodDeployment() {
		this(1, 1, 1, null);
	}

	public PodDeployment(String versionNumber) {
		this(1, 1, versionNumber);
	}
	
	public PodDeployment(int shardCount) {
		this(shardCount, 1, 1);
	}
	
	public PodDeployment(int shardCount, String versionNumber) {
		this(shardCount, 1, 1, versionNumber);
	}

	public PodDeployment(int minReplica, int maxReplica) {
		this(minReplica, maxReplica, null);
	}
	
	public PodDeployment(int minReplica, int maxReplica, String versionNumber) {
		this(1, minReplica, maxReplica, versionNumber);
	}
	
	public PodDeployment(int shardCount, int minReplica, int maxReplica) {
		this(shardCount, minReplica, maxReplica, null);
	}
}
