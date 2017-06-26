/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.messages;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools4j.utils.Utils;

public class RemoteActorMessage<T> extends ActorMessage<T> {
	protected static final long serialVersionUID = 6359184340878978613L;
	
	public RemoteActorMessage(T value, int tag, UUID source, UUID dest) {
		super(value, tag, source, dest);
	}
	
	@SuppressWarnings("unchecked")
	public <C> C convertValue(Class<T> clazz) {
		return (C)(new ObjectMapper().convertValue(value, clazz));
	}

	public boolean valueIsPrimitiveType() {
		return Utils.isWrapperType(value.getClass());
	}
	
	@SuppressWarnings("unchecked")
	public static <C> C convertValue(ActorMessage<?> message, Class<C> clazz) {
		C result = null;
		if ((message instanceof RemoteActorMessage)) {
			message.value = ((RemoteActorMessage<C>)message).convertValue(clazz);
			result = (C)message.value;
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <C> C optionalConvertValue(ActorMessage<?> message, Class<C> clazz) {
		C result = null;
		if ((message instanceof RemoteActorMessage) && !((RemoteActorMessage<?>)message).valueIsPrimitiveType()) {
			message.value = ((RemoteActorMessage<C>)message).convertValue(clazz);
			result = (C)message.value;
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "RemoteActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest
				+ ", byRef=" + byRef + "]";
	}
}
