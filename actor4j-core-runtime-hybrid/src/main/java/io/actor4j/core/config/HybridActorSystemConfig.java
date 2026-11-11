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
package io.actor4j.core.config;

// configuration of the hybrid runtime
public class HybridActorSystemConfig extends ActorSystemConfig {
	// threads of the shared pool (taken from parallelism*parallelismFactor), 0 = automatic (half)
	private final int pooledParallelism;

	public int pooledParallelism() {
		return pooledParallelism;
	}

	public static abstract class Builder<T extends HybridActorSystemConfig> extends ActorSystemConfig.Builder<T> {
		public int pooledParallelism;

		public Builder() {
			super();
		}

		public Builder(T config) {
			super(config);

			this.pooledParallelism = config.pooledParallelism();
		}

		public Builder(ActorSystemConfig config) {
			super(config);

			this.pooledParallelism = config instanceof HybridActorSystemConfig hconfig ? hconfig.pooledParallelism() : 0;
		}

		// the other setters return ActorSystemConfig.Builder: call this one first
		public Builder<T> pooledParallelism(int pooledParallelism) {
			this.pooledParallelism = requirePositive(pooledParallelism, "pooledParallelism");

			return this;
		}
	}

	public HybridActorSystemConfig(Builder<?> builder) {
		super(builder);

		this.pooledParallelism = builder.pooledParallelism;
	}

	public static HybridActorSystemConfig create() {
		return new HybridActorSystemConfig(builder());
	}

	public static Builder<?> builder() {
		return new Builder<HybridActorSystemConfig>() {
			@Override
			public HybridActorSystemConfig build() {
				return new HybridActorSystemConfig(this);
			}
		};
	}

	public static Builder<?> builder(HybridActorSystemConfig config) {
		return new Builder<HybridActorSystemConfig>(config) {
			@Override
			public HybridActorSystemConfig build() {
				return new HybridActorSystemConfig(this);
			}
		};
	}

	public static Builder<?> builder(ActorSystemConfig config) {
		return new Builder<HybridActorSystemConfig>(config) {
			@Override
			public HybridActorSystemConfig build() {
				return new HybridActorSystemConfig(this);
			}
		};
	}
}
