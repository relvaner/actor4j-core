/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
	public static <T> T readValue(String content, Class<T> valueType) {
		T result = null;
		
		try {
			result = new ObjectMapper().readValue(content, valueType);
		} catch (IOException e) {
			e.printStackTrace();
		}			

		return result;
	}
	
	public <T> T readValue(String content, TypeReference<T> valueTypeRef) {
		T result = null;
		
		try {
			result = new ObjectMapper().readValue(content, valueTypeRef);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static String toJson(Object value) {
		String result = null;
		
		try {
			result = new ObjectMapper().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
