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

public interface PodActorMessage<T, U, P> extends ActorMessage<T> {
	public U user();
	public P params();
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest, UUID interaction, U user, P params, String protocol, String domain) {
		return new DefaultPodActorMessage<T, U, P>(value, tag, source, dest, interaction, user, params, protocol, domain);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest) {
		return create(value, tag, source, dest, null, null, null, null, null);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest, String domain) {
		return create(value, tag, source, dest, null, null, null, null, domain);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest, UUID interaction) {
		return create(value, tag, source, dest, interaction, null, null, null, null);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol) {
		return create(value, tag, source, dest, interaction, null, null, protocol, null);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		return create(value, tag, source, dest, interaction, null, null, protocol, domain);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, int tag, UUID source, UUID dest, UUID interaction, U user, String protocol, String domain) {
		return create(value, tag, source, dest, interaction, user, null, protocol, domain);
	}

	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest) {
		return create(value, tag.ordinal(), source, dest);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest, String domain) {
		return create(value, tag.ordinal(), source, dest, domain);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction) {
		return create(value, tag.ordinal(), source, dest, interaction);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, String protocol) {
		return create(value, tag.ordinal(), source, dest, interaction, protocol);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		return create(value, tag.ordinal(), source, dest, interaction, null, null, protocol, domain);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, U user, String protocol, String domain) {
		return create(value, tag.ordinal(), source, dest, interaction, user, null, protocol, domain);
	}
	
	public static <T, U, P> PodActorMessage<T, U, P> create(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction, U user, P params, String protocol, String domain) {
		return create(value, tag.ordinal(), source, dest, interaction, user, params, protocol, domain);
	}
}
