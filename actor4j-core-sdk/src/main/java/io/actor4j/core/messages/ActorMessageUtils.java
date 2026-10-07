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
package io.actor4j.core.messages;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.actor4j.core.utils.Shareable;

public final class ActorMessageUtils {
	public static final Set<Class<?>> SUPPORTED_TYPES;

	public static boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}
	
	// @See Objects.equals
	public static <T> boolean equals(T a, T b) {	
		return (a == b) || (a != null && a.equals(b));
	}

	static {
		SUPPORTED_TYPES = new HashSet<>();

		Collections.addAll(SUPPORTED_TYPES,
			Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
			Character.class, String.class, Boolean.class, Object.class, UUID.class 
		);
	}
	
	public static boolean isShareable(Object value) {
		return value==null
			|| isSupportedType(value.getClass())
			|| value instanceof Record
			|| value instanceof Shareable
			|| value instanceof Enum;
	}
}
