/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
