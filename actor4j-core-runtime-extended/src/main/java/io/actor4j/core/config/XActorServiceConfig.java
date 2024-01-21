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

public class XActorServiceConfig extends XActorSystemConfig {
	public static abstract class Builder<T extends XActorServiceConfig> extends XActorSystemConfig.Builder<T> 	{
		public Builder() {
			super();
			serverMode();
		}
		
		public Builder(T config) {
			super(config);
			serverMode();
		}
	}

	public XActorServiceConfig(Builder<?> builder) {
		super(builder);
	}
	
	public static XActorServiceConfig create() {
		return new XActorServiceConfig(builder());
	}
	
	public static Builder<?> builder() {
		return new Builder<XActorServiceConfig>() {
			@Override
			public XActorServiceConfig build() {
				return new XActorServiceConfig(this);
			}
		};
	}
	
	public static Builder<?> builder(XActorServiceConfig config) {
		return new Builder<XActorServiceConfig>(config) {
			@Override
			public XActorServiceConfig build() {
				return new XActorServiceConfig(this);
			}
		};
	}
}
