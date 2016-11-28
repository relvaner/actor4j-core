/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
