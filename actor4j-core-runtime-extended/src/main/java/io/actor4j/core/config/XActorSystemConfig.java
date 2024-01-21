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

public class XActorSystemConfig extends ActorSystemConfig {
	private final boolean unbounded;
	
	public boolean unbounded() {
		return unbounded;
	}
	
	public static abstract class Builder<T extends XActorSystemConfig> extends ActorSystemConfig.Builder<T> 	{
		public boolean unbounded;
		
		public Builder() {
			super();
			
			this.unbounded = true;
		}
		
		public Builder(T config) {
			super(config);
			
			this.unbounded = config.unbounded();
		}
		
		public Builder<T> unbounded(boolean unbounded) {
			this.unbounded = unbounded;
			
			return this;
		}
	}

	public XActorSystemConfig(Builder<?> builder) {
		super(builder);
		
		this.unbounded = builder.unbounded;
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
}
