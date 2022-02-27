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
package io.actor4j.core.messages;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ActorMessageUtils {
	public static Set<Class<?>> SUPPORTED_TYPES;

	public static boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}

	static {
		SUPPORTED_TYPES = new HashSet<Class<?>>();
		SUPPORTED_TYPES.add(Byte.class);
		SUPPORTED_TYPES.add(Short.class);
		SUPPORTED_TYPES.add(Integer.class);
		SUPPORTED_TYPES.add(Long.class);
		SUPPORTED_TYPES.add(Float.class);
		SUPPORTED_TYPES.add(Double.class);
		SUPPORTED_TYPES.add(Character.class);
		SUPPORTED_TYPES.add(String.class);
		SUPPORTED_TYPES.add(Boolean.class);
		SUPPORTED_TYPES.add(Void.class);
		
		// IMMUTABLE
		SUPPORTED_TYPES.add(Object.class);
		SUPPORTED_TYPES.add(UUID.class);
	}
}
