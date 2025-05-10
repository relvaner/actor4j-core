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

public record ActorIdAsUUID(UUID id) implements ActorId {
	public static ActorIdAsUUID of() {
		return new ActorIdAsUUID(UUID.randomUUID());
	}
	
	public static ActorIdAsUUID of(String id) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(id);
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		return new ActorIdAsUUID(uuid);
	}
	
	public static UUID zero() {
		return UUID.fromString("00000000-0000-0000-0000-000000000000");
	}
	
	public static UUID randomId() {
		return UUID.randomUUID();
	}
	
	@Override
	public String toString() {
		return id.toString();
	}
}
