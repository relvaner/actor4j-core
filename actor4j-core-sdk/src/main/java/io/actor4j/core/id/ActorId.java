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

import io.actor4j.core.utils.Shareable;

public interface ActorId extends Redirect, Shareable {
	public ActorId localId();
	public UUID globalId();
	
	public static ActorId none() {
		return new ActorId() {
			@Override
			public ActorId localId() {
				return null;
			}

			@Override
			public UUID globalId() {
				return null;
			}
			
			@Override
			public ActorId redirectId() {
				return null;
			}
		};
	}
	
	public static ActorId of(UUID globalId) {
		return GlobalId.of(globalId);
	}
	
	public static ActorId ofRedirect(ActorId dest) {
		return Redirect.of(dest);
	}
}
