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
package io.actor4j.core.runtime.config;

public class ActorRuntimeConfig {
	private final InternalServerCallback internalServerCallback;
	private final InternalServerRequest internalServerRequest;
	
	public InternalServerCallback internalServerCallback() {
		return internalServerCallback;
	}
	
	public InternalServerRequest internalServerRequest() {
		return internalServerRequest;
	}
	
	public static abstract class Builder<T extends ActorRuntimeConfig> {
		protected InternalServerCallback internalServerCallback;
		protected InternalServerRequest internalServerRequest;
		
		public Builder() {
			super();
			
			internalServerCallback = null;
			internalServerRequest  = null;
		}
		
		public Builder(T config) {
			super();
			this.internalServerCallback = config.internalServerCallback();
			this.internalServerRequest = config.internalServerRequest();
		}
		
		public Builder<T> internalServerCallback(InternalServerCallback internalServerCallback) {
			this.internalServerCallback = internalServerCallback;
			
			return this;
		}
		
		public Builder<T> internalServerRequest(InternalServerRequest internalServerRequest) {
			this.internalServerRequest = internalServerRequest;
			
			return this;
		}
		
		public abstract T build();
	}
	
	public ActorRuntimeConfig(Builder<?> builder) {
		super();
		this.internalServerCallback = builder.internalServerCallback;
		this.internalServerRequest = builder.internalServerRequest;
	}
	
	public static ActorRuntimeConfig create() {
		return new ActorRuntimeConfig(builder());
	}
	
	public static Builder<?> builder() {
		return new Builder<ActorRuntimeConfig>() {
			@Override
			public ActorRuntimeConfig build() {
				return new ActorRuntimeConfig(this);
			}
		};
	}
	
	public static Builder<?> builder(ActorRuntimeConfig config) {
		return new Builder<ActorRuntimeConfig>(config) {
			@Override
			public ActorRuntimeConfig build() {
				return new ActorRuntimeConfig(this);
			}
		};
	}
}
