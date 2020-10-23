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
package io.actor4j.core.messages;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.actor4j.core.utils.Copyable;
import io.actor4j.core.utils.Shareable;

public class ActorMessage<T> implements Copyable<ActorMessage<T>>, Comparable<ActorMessage<T>> {
	private static Set<Class<?>> SUPPORTED_TYPES;
	
	public T value;
	public int tag;
	public UUID source;
	public UUID dest;
	
	public UUID interaction;
	public String protocol;
	public String domain;
	
	public ActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		this.value = value;
		this.tag = tag;
		this.source = source;
		this.dest = dest;
		this.interaction = interaction;
		this.protocol = protocol;
		this.domain = domain;
	}

	public ActorMessage(T value, int tag, UUID source, UUID dest) {
		this(value, tag, source, dest, null, null, null);
	}

	public ActorMessage(T value, Enum<?> tag, UUID source, UUID dest) {
		this(value, tag.ordinal(), source, dest);
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public UUID getSource() {
		return source;
	}

	public void setSource(UUID source) {
		this.source = source;
	}

	public UUID getDest() {
		return dest;
	}

	public void setDest(UUID dest) {
		this.dest = dest;
	}
	
	public UUID getInteraction() {
		return interaction;
	}

	public void setInteraction(UUID interaction) {
		this.interaction = interaction;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public boolean valueAsBoolean() {
		return (Boolean)value;
	}
	
	public int valueAsInt() {
		return (Integer)value;
	}
	
	public long valueAsLong() {
		return (Long)value;
	}
	
	public double valueAsDouble() {
		return (Double)value;
	}
	
	public String valueAsString() {
		return (String)value;
	}
	
	public UUID valueAsUUID() {
		return (UUID)value;
	}
	
	public T readValue(Class<T> valueType) {
		T result = null;
		
		if (value instanceof String) // as json string
			try {
				result = new ObjectMapper().readValue((String)value, valueType);
			} catch (IOException e) {
				e.printStackTrace();
			}

		return result;
	}
	
	public T readValue(TypeReference<T> valueTypeRef) {
		T result = null;
		
		if (value instanceof String) // as json string
			try {
				result = new ObjectMapper().readValue((String)value, valueTypeRef);
			} catch (IOException e) {
				e.printStackTrace();
			}

		return result;
	}
	
	protected ActorMessage<T> weakCopy() {
		return new ActorMessage<T>(value, tag, source, dest, interaction, protocol, domain);
	}
	
	@SuppressWarnings("unchecked")
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (isSupportedType(value.getClass()) || value instanceof Shareable)
				return new ActorMessage<T>(value, tag, source, dest, interaction, protocol, domain);
			else if (value instanceof Copyable)
				return new ActorMessage<T>(((Copyable<T>)value).copy(), tag, source, dest, interaction, protocol, domain);
			else if (value instanceof Exception)
				return new ActorMessage<T>(value, tag, source, dest, interaction, protocol, domain);
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return new ActorMessage<T>(null, tag, source, dest, interaction, protocol, domain);
	}
	
	@Override
	public int compareTo(ActorMessage<T> message) {
		return Integer.compare(tag, message.tag); // tag - message.tag
	}

	@Override
	public String toString() {
		return "ActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest
				+ ", interaction=" + interaction + ", protocol=" + protocol + ", domain=" + domain + "]";
	}

	public static boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}

	static {
		SUPPORTED_TYPES = new HashSet<Class<?>>();
		SUPPORTED_TYPES.add(Byte.class);
		SUPPORTED_TYPES.add(Short.class);
		SUPPORTED_TYPES.add(Integer.class);
		SUPPORTED_TYPES.add(Long.class);
		SUPPORTED_TYPES.add(Float.class);
		SUPPORTED_TYPES.add(Double.class);
		SUPPORTED_TYPES.add(Character.class);
		SUPPORTED_TYPES.add(String.class);
		SUPPORTED_TYPES.add(Boolean.class);
		SUPPORTED_TYPES.add(Void.class);
		
		// IMMUTABLE
		SUPPORTED_TYPES.add(Object.class);
		SUPPORTED_TYPES.add(UUID.class);
	}
}
