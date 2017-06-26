/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
