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

import java.util.Objects;
import java.util.function.Predicate;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorDistributedGroupMember;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.actors.PooledActor;

// configuration of the hybrid runtime: thread-bound threads (default pool) and pooled threads (classic pool).
// Keep threadBound + pooled <= logical cores: busy thread-bound threads would otherwise delay the pooled ones
// (measured: 16 physical / 32 logical cores, 32 thread-bound + pool -> coordinators wait for the OS scheduler).
public class HybridActorSystemConfig extends ActorSystemConfig {
	// placement: which actors run in the shared pool (root cells always stay thread-bound)
	public static final Predicate<Actor> POOLED_MARKER = (actor) -> actor instanceof PooledActor;
	// all actors except members of thread-bound groups (e.g. watchdog actors)
	public static final Predicate<Actor> POOLED_ALL = (actor) -> !(actor instanceof ActorGroupMember || actor instanceof ActorDistributedGroupMember);

	// default: POOLED_MARKER; e.g. by name or package for actors whose code cannot be changed
	private final Predicate<Actor> pooledPlacement;
	// thread-bound threads, 0 = parallelism*parallelismFactor (parallelism defaults to half of the cores here)
	private final int threadBoundParallelism;
	// pooled threads in addition, 0 = max(2, thread-bound/2); depends on the scenario: light coordinators that mostly
	// wait (scatter-gather) need few threads, CPU-heavy pooled work (fork/join like) more
	private final int pooledParallelism;

	public int threadBoundParallelism() {
		return threadBoundParallelism;
	}

	public int pooledParallelism() {
		return pooledParallelism;
	}

	public Predicate<Actor> pooledPlacement() {
		return pooledPlacement;
	}

	public static Predicate<Actor> pooledPlacement(ActorSystemConfig config) {
		return config instanceof HybridActorSystemConfig hconfig ? hconfig.pooledPlacement() : POOLED_MARKER;
	}

	public static int threadBoundThreads(ActorSystemConfig config) {
		if (config instanceof HybridActorSystemConfig hconfig && hconfig.threadBoundParallelism()>0)
			return hconfig.threadBoundParallelism();
		return config.parallelism()*config.parallelismFactor();
	}

	public static int pooledThreads(ActorSystemConfig config) {
		if (config instanceof HybridActorSystemConfig hconfig && hconfig.pooledParallelism()>0)
			return hconfig.pooledParallelism();
		return Math.max(2, threadBoundThreads(config)/2);
	}

	public static abstract class Builder<T extends HybridActorSystemConfig> extends ActorSystemConfig.Builder<T> {
		public int threadBoundParallelism;
		public int pooledParallelism;
		public Predicate<Actor> pooledPlacement = POOLED_MARKER;

		public Builder() {
			super();

			parallelism(Math.max(1, Runtime.getRuntime().availableProcessors()/2)); // room for the pooled threads
		}

		public Builder(T config) {
			super(config);

			this.threadBoundParallelism = config.threadBoundParallelism();
			this.pooledParallelism = config.pooledParallelism();
			this.pooledPlacement = config.pooledPlacement();
		}

		public Builder(ActorSystemConfig config) {
			super(config);

			this.threadBoundParallelism = config instanceof HybridActorSystemConfig hconfig ? hconfig.threadBoundParallelism() : 0;
			this.pooledParallelism = config instanceof HybridActorSystemConfig hconfig ? hconfig.pooledParallelism() : 0;
			this.pooledPlacement = HybridActorSystemConfig.pooledPlacement(config);
		}

		// the inherited setters return ActorSystemConfig.Builder: call the hybrid setters first
		public Builder<T> threadBoundParallelism(int threadBoundParallelism) {
			this.threadBoundParallelism = requirePositive(threadBoundParallelism, "threadBoundParallelism");

			return this;
		}

		public Builder<T> pooledParallelism(int pooledParallelism) {
			this.pooledParallelism = requirePositive(pooledParallelism, "pooledParallelism");

			return this;
		}

		public Builder<T> pooledPlacement(Predicate<Actor> pooledPlacement) {
			this.pooledPlacement = Objects.requireNonNull(pooledPlacement, "pooledPlacement");

			return this;
		}
	}

	public HybridActorSystemConfig(Builder<?> builder) {
		super(builder);

		this.threadBoundParallelism = builder.threadBoundParallelism;
		this.pooledParallelism = builder.pooledParallelism;
		this.pooledPlacement = builder.pooledPlacement;
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
