/*
 * Copyright (c) 2015-2022, David A. Bauer. All rights reserved.
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

public interface ActorMessage<T> extends Comparable<ActorMessage<T>> {
	public T value();
	public int tag();
	public UUID source();
	public UUID dest();
	public UUID interaction();
	public String protocol();
	public String domain();
	
	public default boolean valueAsBoolean() {
		return (Boolean)value();
	}
	
	public default int valueAsInt() {
		return (Integer)value();
	}
	
	public default long valueAsLong() {
		return (Long)value();
	}
	
	public default double valueAsDouble() {
		return (Double)value();
	}
	
	public default String valueAsString() {
		return (String)value();
	}
	
	public default UUID valueAsUUID() {
		return (UUID)value();
	}
	
	public default boolean isSelfReferencing() {
		return source().equals(dest());
	}
	
	public default boolean isSelfReferencing(UUID self) {
		return source().equals(self) && dest().equals(self);
	}
	
	public ActorMessage<T> shallowCopy();
	public ActorMessage<T> shallowCopy(int tag);
	public ActorMessage<T> shallowCopy(UUID source, UUID dest);
	public ActorMessage<T> shallowCopy(UUID dest);
	
	public ActorMessage<T> copy();
	public ActorMessage<T> copy(UUID dest);
	
	public default int compareTo(ActorMessage<T> message) {
		return Integer.compare(tag(), message.tag()); // tag - message.tag
	}
	
	public static <T> ActorMessage<T> create(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		return new DefaultActorMessage<T>(value, tag, source, dest, interaction, protocol, domain);
	}
	
	public static <T> ActorMessage<T> create(T value, int tag, UUID source, UUID dest) {
		return create(value, tag, source, dest, null, null, null);
	}
	
	public static <T> ActorMessage<T> create(T value, int tag, UUID source, UUID dest, String domain) {
		return create(value, tag, source, dest, null, null, domain);
	}
	
	public static <T> ActorMessage<T> create(T value, int tag, UUID source, UUID dest, UUID interaction) {
		return create(value, tag, source, dest, interaction, null, null);
	}
	
	public static <T> ActorMessage<T> create(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol) {
		return create(value, tag, source, dest, interaction, protocol, null);
	}

	public static <T> ActorMessage<T> create(T value, Enum<?> tag, UUID source, UUID dest) {
		return create(value, tag.ordinal(), source, dest);
	}
	
	public static <T> ActorMessage<T> create(T value, Enum<?> tag, UUID source, UUID dest, String domain) {
		return create(value, tag.ordinal(), source, dest, domain);
	}
	
	public static <T> ActorMessage<T> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction) {
		return create(value, tag.ordinal(), source, dest, interaction);
	}
	
	public static <T> ActorMessage<T> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, String protocol) {
		return create(value, tag.ordinal(), source, dest, interaction, protocol);
	}
	
	public static <T> ActorMessage<T> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		return create(value, tag.ordinal(), source, dest, interaction, protocol, domain);
	}
}
