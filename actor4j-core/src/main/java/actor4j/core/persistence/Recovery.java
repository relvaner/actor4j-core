/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.persistence;

import com.fasterxml.jackson.core.type.TypeReference;

public class Recovery<S extends ActorPersistenceObject, E extends ActorPersistenceObject> extends ActorPersistenceRecoveryObject<S, E>{
	public Recovery() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static <A extends ActorPersistenceObject, B extends ActorPersistenceObject> Recovery<A, B> convertValue(String json, TypeReference<?> valueTypeRef) {
		return (Recovery<A, B>)ActorPersistenceRecoveryObject.convertValue(json, valueTypeRef);
	}

	@Override
	public String toString() {
		return "Recovery [state=" + state + ", events=" + events + "]";
	}
}
