/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Utils {
	private static Set<Class<?>> PRIMITIVE_WRAPPERS;
	private static Map<Class<?>, Class<?>> PRIMITIVE_MAPPERS;
	
	public static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive();
	}
	
	public static boolean isWrapperType(Class<?> type) {
		return PRIMITIVE_WRAPPERS.contains(type);
	}
	
	public static Class<?> getPrimitiveWrapper(Class<?> type) {
		return PRIMITIVE_MAPPERS.get(type);
	}
	
	static {
		PRIMITIVE_WRAPPERS = new HashSet<Class<?>>();
		PRIMITIVE_WRAPPERS.add(Byte.class);
		PRIMITIVE_WRAPPERS.add(Short.class);
		PRIMITIVE_WRAPPERS.add(Integer.class);
		PRIMITIVE_WRAPPERS.add(Long.class);
		PRIMITIVE_WRAPPERS.add(Float.class);
		PRIMITIVE_WRAPPERS.add(Double.class);
		PRIMITIVE_WRAPPERS.add(Character.class);
		PRIMITIVE_WRAPPERS.add(String.class);
		PRIMITIVE_WRAPPERS.add(Boolean.class);
		PRIMITIVE_WRAPPERS.add(Void.class);
		
		PRIMITIVE_MAPPERS = new HashMap<>();
		PRIMITIVE_MAPPERS.put(byte.class, Byte.class);
		PRIMITIVE_MAPPERS.put(short.class, Short.class);
		PRIMITIVE_MAPPERS.put(int.class, Integer.class);
		PRIMITIVE_MAPPERS.put(long.class, Long.class);
		PRIMITIVE_MAPPERS.put(float.class, Float.class);
		PRIMITIVE_MAPPERS.put(double.class, Double.class);
		PRIMITIVE_MAPPERS.put(char.class, Character.class);
		PRIMITIVE_MAPPERS.put(String.class, String.class);
		PRIMITIVE_MAPPERS.put(boolean.class, Boolean.class);
		PRIMITIVE_MAPPERS.put(Void.class, Void.class);
	}
}
