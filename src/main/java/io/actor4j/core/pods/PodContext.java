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

public class PodContext {
	protected final String domain;
	protected final boolean isShard;
	protected final String shardId;
	protected final boolean primaryReplica;
	
	public PodContext() {
		super();
		this.domain = null;
		this.isShard = false;
		this.shardId = null;
		this.primaryReplica = false;
	}
	
	public PodContext(String domain, boolean isShard, String shardId, boolean primaryReplica) {
		super();
		this.domain = domain;
		this.isShard = isShard;
		this.shardId = shardId;
		this.primaryReplica = primaryReplica;
	}

	public String getDomain() {
		return domain;
	}

	public boolean isShard() {
		return isShard;
	}

	public String getShardId() {
		return shardId;
	}

	public boolean isPrimaryReplica() {
		return primaryReplica;
	}
	
	public boolean hasPrimaryReplica() {
		return true; // temporary
	}
}
