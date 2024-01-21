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
import java.util.Map;

public final class Utils {
	private static Map<Class<?>, Class<?>> PRIMITIVE_MAPPERS;
	
	public static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive();
	}
	
	public static Class<?> getPrimitiveWrapper(Class<?> type) {
		return PRIMITIVE_MAPPERS.get(type);
	}
	
	static {
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
