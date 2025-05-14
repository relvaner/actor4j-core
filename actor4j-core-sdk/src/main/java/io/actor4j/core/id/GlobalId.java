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
package io.actor4j.core.id;

import java.util.UUID;

public record GlobalId(ActorId localId, UUID globalId) implements ActorId {
	public static final UUID UUID_ZERO = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	public GlobalId(UUID globalId) {
		this(null, globalId);
	}
	
	public static ActorId of(UUID globalId) {
		return new GlobalId(globalId);
	}
	
	public static ActorId random() {
		return new GlobalId(UUID.randomUUID());
	}
	
	public static ActorId zero() {
		return new GlobalId(UUID_ZERO);
	}
}
