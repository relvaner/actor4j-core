/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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

import io.actor4j.core.config.XActorSystemConfig;
import io.actor4j.core.runtime.extended.XDefaultActorSystemImpl;

public class XActorRuntime {
	public static ActorSystemFactory factory() {
		return (c) -> new XDefaultActorSystemImpl((XActorSystemConfig)c);
	}
	
	public static ActorSystem create() {
		return create(XActorSystemConfig.create());
	}
	
	public static ActorSystem create(String name) {
		return create(XActorSystemConfig.builder().name(name).build());
	}
	
	public static ActorSystem create(XActorSystemConfig config) {
		return factory().apply(config);
	}
}
