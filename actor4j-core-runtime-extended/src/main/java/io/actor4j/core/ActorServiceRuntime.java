/*
 * Copyright (c) 2015-2025, David A. Bauer. All rights reserved.
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
import io.actor4j.core.config.XActorServiceConfig;

public class ActorServiceRuntime {
	public static ActorSystemFactory factory() {
		return XActorServiceRuntime.factory();
	}
	
	public static ActorService create() {
		return XActorServiceRuntime.create();
	}
	
	public static ActorService create(String name) {
		return XActorServiceRuntime.create(name);
	}
	
	public static ActorService create(ActorSystemConfig config) {
		return XActorServiceRuntime.create(config instanceof XActorServiceConfig ? 
			(XActorServiceConfig)config : (XActorServiceConfig) XActorServiceConfig.builder(config).build());
	}
}
