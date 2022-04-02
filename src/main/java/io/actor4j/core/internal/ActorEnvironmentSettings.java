/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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
package io.actor4j.core.internal;

import java.util.UUID;

import io.actor4j.core.ActorSystemFactory;
import io.actor4j.core.function.TriConsumer;

public class ActorEnvironmentSettings {
	// @See: ActorSystem, ActorService
	public static ActorSystemFactory defaultFactory = (c) -> new DefaultActorSystemImpl(c);
	
	// @See: RemoteHandlerPodActor, RemoteFunctionPod
	public static TriConsumer<String, Object, Integer> internal_server_callback;
	// @See: RemoteHandlerPodActor, RemoteFunctionPod
	public static TriConsumer<Object, UUID, String> internal_server_request;
}
