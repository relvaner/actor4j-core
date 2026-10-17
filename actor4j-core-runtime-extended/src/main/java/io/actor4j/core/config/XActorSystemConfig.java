/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.config;

import io.actor4j.core.runtime.annotations.Reserved;

public class XActorSystemConfig extends ActorSystemConfig {
	private final boolean unbounded;
	@Reserved(reason="experimental")
	private final boolean antiFlooding;
	
	public boolean unbounded() {
		return unbounded;
	}
	
	public boolean antiFlooding() {
		return antiFlooding;
	}
	
	public static abstract class Builder<T extends XActorSystemConfig> extends ActorSystemConfig.Builder<T> 	{
		public boolean unbounded;
		public boolean antiFlooding;
		
		public Builder() {
			super();
			
			this.unbounded = true;
			this.antiFlooding = false;
		}
		
		public Builder(T config) {
			super(config);
			
			this.unbounded = config.unbounded();
			this.antiFlooding = config.antiFlooding();
		}
		
		public Builder(ActorSystemConfig config) {
			super(config);
			
			this.unbounded = config instanceof XActorSystemConfig xconfig ? xconfig.unbounded() : true;
			this.antiFlooding = config instanceof XActorSystemConfig xconfig ? xconfig.antiFlooding() : false;
		}
		
		public Builder<T> unbounded(boolean unbounded) {
			this.unbounded = unbounded;
			
			return this;
		}
		
		public Builder<T> antiFlooding(boolean antiFlooding) {
			this.antiFlooding = antiFlooding;
			
			return this;
		}
	}

	public XActorSystemConfig(Builder<?> builder) {
		super(builder);
		
		this.unbounded = builder.unbounded;
		this.antiFlooding = builder.antiFlooding;
	}
	
	public static XActorSystemConfig create() {
		return new XActorSystemConfig(builder());
	}
	
	public static Builder<?> builder() {
		return new Builder<XActorSystemConfig>() {
			@Override
			public XActorSystemConfig build() {
				return new XActorSystemConfig(this);
			}
		};
	}
	
	public static Builder<?> builder(XActorSystemConfig config) {
		return new Builder<XActorSystemConfig>(config) {
			@Override
			public XActorSystemConfig build() {
				return new XActorSystemConfig(this);
			}
		};
	}
	
	public static Builder<?> builder(ActorSystemConfig config) {
		return new Builder<XActorSystemConfig>(config) {
			@Override
			public XActorSystemConfig build() {
				return new XActorSystemConfig(this);
			}
		};
	}
}
