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

import java.util.function.Function;

public final class PodContext {
	private final String domain; 
	private final boolean isShard; 
	private final String shardId; 
	private final boolean primaryReplica; 
	private final Function<String, Boolean> function;

	public PodContext(String domain, boolean isShard, String shardId, boolean primaryReplica,
			Function<String, Boolean> function) {
		super();
		this.domain = domain;
		this.isShard = isShard;
		this.shardId = shardId;
		this.primaryReplica = primaryReplica;
		this.function = function;
	}

	/**
	 * Exists on this actor system a running primary replica of this pod?
	 */
	public boolean hasPrimaryReplica() {
		return function.apply(domain);
	}
	
	public String domain() {
		return domain;
	}
	
	public boolean isShard() {
		return isShard;
	}
	
	public String shardId() {
		return shardId;
	}
	
	public boolean primaryReplica() {
		return primaryReplica;
	}
	
	public Function<String, Boolean> function() {
		return function;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((function == null) ? 0 : function.hashCode());
		result = prime * result + (isShard ? 1231 : 1237);
		result = prime * result + (primaryReplica ? 1231 : 1237);
		result = prime * result + ((shardId == null) ? 0 : shardId.hashCode());
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
		PodContext other = (PodContext) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (isShard != other.isShard)
			return false;
		if (primaryReplica != other.primaryReplica)
			return false;
		if (shardId == null) {
			if (other.shardId != null)
				return false;
		} else if (!shardId.equals(other.shardId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PodContext [domain=" + domain + ", isShard=" + isShard + ", shardId=" + shardId + ", primaryReplica="
				+ primaryReplica + ", function=" + function + "]";
	}
}
