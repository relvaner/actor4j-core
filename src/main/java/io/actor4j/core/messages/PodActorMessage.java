/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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

import io.actor4j.core.utils.Copyable;
import io.actor4j.core.utils.Shareable;

public class PodActorMessage<T, A> extends ActorMessage<T> {
	public final A authentication;

	public PodActorMessage(T value, Enum<?> tag, UUID source, UUID dest, String domain) {
		this(value, tag.ordinal(), source, dest, domain);
	}

	public PodActorMessage(T value, Enum<?> tag, UUID source, UUID dest, UUID interaction) {
		this(value, tag.ordinal(), source, dest, interaction);
	}

	public PodActorMessage(T value, Enum<?> tag, UUID source, UUID dest) {
		this(value, tag.ordinal(), source, dest);
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest, String domain) {
		this(value, tag, source, dest, null, null, null, domain);
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction, A authentication, String protocol, String domain) {
		super(value, tag, source, dest, interaction, protocol, domain);
		this.authentication = authentication;
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction, A authentication) {
		this(value, tag, source, dest, interaction, authentication, null, null);
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction) {
		this(value, tag, source, dest, interaction, null, null, null);
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest) {
		this(value, tag, source, dest, null, null, null, null);
	}

	public A getAuthentication() {
		return authentication;
	}
	
	protected ActorMessage<T> weakCopy() {
		return new PodActorMessage<T, A>(value, tag, source, dest, interaction, authentication, protocol, domain);
	}
	
	@SuppressWarnings("unchecked")
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (isSupportedType(value.getClass()) || value instanceof Shareable)
				return new PodActorMessage<T, A>(value, tag, source, dest, interaction, authentication, protocol, domain);
			else if (value instanceof Copyable)
				return new PodActorMessage<T, A>(((Copyable<T>)value).copy(), tag, source, dest, interaction, authentication, protocol, domain);
			else if (value instanceof Exception)
				return new PodActorMessage<T, A>(value, tag, source, dest, interaction, authentication, protocol, domain);
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return new PodActorMessage<T, A>(null, tag, source, dest, interaction, authentication, protocol, domain);
	}
}
