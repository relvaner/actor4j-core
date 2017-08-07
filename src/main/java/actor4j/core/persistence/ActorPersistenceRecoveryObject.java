/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package actor4j.core.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActorPersistenceRecoveryObject<S extends ActorPersistenceObject, E extends ActorPersistenceObject> {
	public S state;
	public List<E> events;
	 
	public ActorPersistenceRecoveryObject() {
		super();
		events = new ArrayList<>();
	}
	
	public static boolean isError(String json) {
		boolean result = false;
		try {
			JSONObject obj = new JSONObject(json);
			result = obj.has("error");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String getErrorMsg(String json) {
		String result = null;
		try {
			JSONObject obj = new JSONObject(json);
			result = obj.getString("error");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends ActorPersistenceObject, B extends ActorPersistenceObject> ActorPersistenceRecoveryObject<A, B> convertValue(String json, TypeReference<?> valueTypeRef) {
		ActorPersistenceRecoveryObject<A, B> result = null;
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			result = (ActorPersistenceRecoveryObject<A, B>)objectMapper.readValue(json, valueTypeRef);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "ActorPersistenceRecoveryObject [state=" + state + ", events=" + events + "]";
	}
}
