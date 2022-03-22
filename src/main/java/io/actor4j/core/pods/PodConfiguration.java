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

public final class PodConfiguration {
	private final String domain; 
	private final String className; 
	private final int shardCount; 
	private final int minReplica; 
	private final int maxReplica; 
	private final String versionNumber;

	public PodConfiguration(String domain, String className, int shardCount, int minReplica, int maxReplica,
			String versionNumber) {
		super();
		this.domain = domain;
		this.className = className;
		this.shardCount = shardCount;
		this.minReplica = minReplica;
		this.maxReplica = maxReplica;
		this.versionNumber = versionNumber;
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
	
	public String domain() {
		return domain;
	}
	
	public String className() {
		return className;
	}
	
	public int shardCount() {
		return shardCount;
	}
	
	public int minReplica() {
		return minReplica;
	}
	
	public int maxReplica() {
		return maxReplica;
	}
	
	public String versionNumber() {
		return versionNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + maxReplica;
		result = prime * result + minReplica;
		result = prime * result + shardCount;
		result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
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
		PodConfiguration other = (PodConfiguration) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (maxReplica != other.maxReplica)
			return false;
		if (minReplica != other.minReplica)
			return false;
		if (shardCount != other.shardCount)
			return false;
		if (versionNumber == null) {
			if (other.versionNumber != null)
				return false;
		} else if (!versionNumber.equals(other.versionNumber))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PodConfiguration [domain=" + domain + ", className=" + className + ", shardCount=" + shardCount
				+ ", minReplica=" + minReplica + ", maxReplica=" + maxReplica + ", versionNumber=" + versionNumber
				+ "]";
	}
}
