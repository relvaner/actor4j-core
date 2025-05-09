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
package io.actor4j.core.runtime;

import java.util.UUID;

import io.actor4j.core.function.SextConsumer;
import io.actor4j.core.function.TriConsumer;
import io.actor4j.core.id.ActorId;

public class ActorGlobalSettings {
	// @See: RemoteHandlerPodActor, RemoteFunctionPod
	public static TriConsumer<String, Object, Integer> internal_server_callback;
	// @See: RemoteHandlerPodActor, RemoteFunctionPod
	public static SextConsumer<Object, Integer, ActorId, UUID, Object, ActorId> internal_server_request;
}
