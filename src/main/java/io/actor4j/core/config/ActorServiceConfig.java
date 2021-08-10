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

public class ActorServiceConfig extends ActorSystemConfig {
	public static abstract class Builder<T extends ActorServiceConfig> extends ActorSystemConfig.Builder<T> 	{
		public Builder() {
			super();
			serverMode();
		}
		
		public Builder(T config) {
			super(config);
			serverMode();
		}
	}

	public ActorServiceConfig(Builder<?> builder) {
		super(builder);
	}
	
	public static ActorServiceConfig create() {
		return new ActorServiceConfig(builder());
	}
	
	public static Builder<?> builder() {
		return new Builder<ActorServiceConfig>() {
			@Override
			public ActorServiceConfig build() {
				return new ActorServiceConfig(this);
			}
		};
	}
	
	public static Builder<?> builder(ActorServiceConfig config) {
		return new Builder<ActorServiceConfig>(config) {
			@Override
			public ActorServiceConfig build() {
				return new ActorServiceConfig(this);
			}
		};
	}
}
