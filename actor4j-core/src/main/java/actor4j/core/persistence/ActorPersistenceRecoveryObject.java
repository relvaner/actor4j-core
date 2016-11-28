/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ActorPersistenceRecoveryObject<S extends ActorPersistenceObject, E extends ActorPersistenceObject> {
	public S state;
	public List<E> events;
	 
	public ActorPersistenceRecoveryObject() {
		super();
		events = new ArrayList<>();
	}
	
	public static <R> R convertValue(String json, Class<R> clazz) {
		return (R)(new ObjectMapper().convertValue(json, clazz));
	}

	@Override
	public String toString() {
		return "ActorPersistenceRecoveryObject [state=" + state + ", events=" + events + "]";
	}
}
