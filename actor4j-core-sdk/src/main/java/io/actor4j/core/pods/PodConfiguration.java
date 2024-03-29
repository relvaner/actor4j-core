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
package io.actor4j.core.pods;

public record PodConfiguration(String domain, String className, int shardCount, int minReplica, int maxReplica,
		String versionNumber) {
	
	public PodConfiguration(String domain, String className) {
		this(domain, className, 1, 1, null);
	}
	
	public PodConfiguration(String domain, String className, String versionNumber) {
		this(domain, className, 1, 1, versionNumber);
	}
	
	public PodConfiguration(String domain, String className, int shardCount) {
		this(domain, className, shardCount, 1, 1);
	}
	
	public PodConfiguration(String domain, String className, int shardCount, String versionNumber) {
		this(domain, className, shardCount, 1, 1, versionNumber);
	}

	public PodConfiguration(String domain, String className, int minReplica, int maxReplica) {
		this(domain, className, minReplica, maxReplica, null);
	}
	
	public PodConfiguration(String domain, String className, int minReplica, int maxReplica, String versionNumber) {
		this(domain, className, 1, minReplica, maxReplica, versionNumber);
	}
	
	public PodConfiguration(String domain, String className, int shardCount, int minReplica, int maxReplica) {
		this(domain, className, shardCount, minReplica, maxReplica, null);
	}
}
