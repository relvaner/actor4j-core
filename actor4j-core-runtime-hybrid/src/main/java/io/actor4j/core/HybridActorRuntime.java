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
package io.actor4j.core;

import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.config.HybridActorSystemConfig;
import io.actor4j.core.runtime.hybrid.HybridActorSystemImpl;

public class HybridActorRuntime {
	// actor4j.hybrid.placement: marker (default, PooledActor) or pooled (all actors except thread-bound groups;
	// watchdog off like in the classic runtime, it only watches the thread-bound workers)
	public static ActorSystemFactory factory() {
		if ("pooled".equals(System.getProperty("actor4j.hybrid.placement", "marker")))
			return (c) -> new HybridActorSystemImpl(HybridActorSystemConfig.builder(c).watchdogEnabled(false).build(), HybridActorSystemImpl.POOLED_ALL);
		return (c) -> new HybridActorSystemImpl(c);
	}

	public static ActorSystem create() {
		return create(HybridActorSystemConfig.create());
	}

	public static ActorSystem create(String name) {
		return create(HybridActorSystemConfig.builder().name(name).build());
	}

	public static ActorSystem create(ActorSystemConfig config) {
		return factory().apply(config);
	}
}
