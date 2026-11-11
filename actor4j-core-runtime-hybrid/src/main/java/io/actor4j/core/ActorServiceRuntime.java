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

// facade of the hybrid runtime (same name as in the other runtime modules, this module comes first on the classpath)
public class ActorServiceRuntime {
	public static ActorSystemFactory factory() {
		return HybridActorServiceRuntime.factory();
	}

	public static ActorService create() {
		return HybridActorServiceRuntime.create();
	}

	public static ActorService create(String name) {
		return HybridActorServiceRuntime.create(name);
	}

	public static ActorService create(ActorSystemConfig config) {
		return HybridActorServiceRuntime.create(config);
	}
}
