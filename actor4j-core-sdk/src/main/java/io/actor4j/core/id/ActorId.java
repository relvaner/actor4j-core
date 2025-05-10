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

public interface ActorId {
	Object id();
	
	public static ActorIdAsLong ofLong() {
		return ActorIdAsLong.of();
	}
	
	public static ActorIdAsLong ofLong(Long id) {
		return ActorIdAsLong.of(id);
	}
	
	public static ActorIdAsLong ofLong(String id) {
		return ActorIdAsLong.of(Long.valueOf(id));
	}
	
	public static ActorIdAsUUID ofUUID() {
		return ActorIdAsUUID.of();
	}
	
	public static ActorIdAsUUID ofUUID(String id) {
		return ActorIdAsUUID.of(id);
	}
}
