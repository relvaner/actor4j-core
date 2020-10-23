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
package io.actor4j.core.messages;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.actor4j.core.utils.Utils;

public class RemoteActorMessage<T> extends ActorMessage<T> {
	public RemoteActorMessage(T value, int tag, UUID source, UUID dest) {
		super(value, tag, source, dest);
	}
		
	public RemoteActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		super(value, tag, source, dest, interaction, protocol, domain);
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
				+ ", interaction=" + interaction + ", protocol=" + protocol + ", domain=" + domain + "]";
	}
}
