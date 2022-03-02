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

import io.actor4j.core.utils.DeepCopyable;
import io.actor4j.core.utils.Shareable;

public record PodActorMessage<T, U>(T value, int tag, UUID source, UUID dest, UUID interaction, U user, String protocol, String domain) implements ActorMessage<T> {
	public PodActorMessage {
		// empty
	}
	
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

	public PodActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction, U user) {
		this(value, tag, source, dest, interaction, user, null, null);
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest, UUID interaction) {
		this(value, tag, source, dest, interaction, null, null, null);
	}

	public PodActorMessage(T value, int tag, UUID source, UUID dest) {
		this(value, tag, source, dest, null, null, null, null);
	}
	
	@Override
	public ActorMessage<T> shallowCopy() {
		return new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain);
	}
	
	@Override
	public ActorMessage<T> shallowCopy(T value) {
		return !ActorMessageUtils.equals(this.value, value) ? 
			new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
	}
	
	@Override
	public ActorMessage<T> shallowCopy(int tag) {
		return this.tag!=tag ? 
			new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
	}
	
	@Override
	public ActorMessage<T> shallowCopy(T value, int tag) {
		return !ActorMessageUtils.equals(this.value, value) || this.tag!=tag ? 
			new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
	}
	
	@Override
	public ActorMessage<T> shallowCopy(int tag, String protocol) {
		return this.tag!=tag || !ActorMessageUtils.equals(this.protocol, protocol) ? 
			new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
	}
	
	@Override
	public ActorMessage<T> shallowCopy(UUID source, UUID dest) {
		return !ActorMessageUtils.equals(this.source, source) || !ActorMessageUtils.equals(this.dest, dest) ? 
			new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
	}
	
	@Override
	public ActorMessage<T> shallowCopy(UUID dest) {
		return !ActorMessageUtils.equals(this.dest, dest) ? 
			new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (ActorMessageUtils.isSupportedType(value.getClass()) || value instanceof Shareable)
				return this;
			else if (value instanceof DeepCopyable)
				return new PodActorMessage<T, U>(((DeepCopyable<T>)value).deepCopy(), tag, source, dest, interaction, user, protocol, domain);
			else if (value instanceof Exception)
				return this;
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy(UUID dest) {
		if (value!=null) { 
			if (ActorMessageUtils.isSupportedType(value.getClass()) || value instanceof Shareable)
				return !ActorMessageUtils.equals(this.dest, dest) ? new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
			else if (value instanceof DeepCopyable)
				return new PodActorMessage<T, U>(((DeepCopyable<T>)value).deepCopy(), tag, source, dest, interaction, user, protocol, domain);
			else if (value instanceof Exception)
				return !ActorMessageUtils.equals(this.dest, dest) ? new PodActorMessage<T, U>(value, tag, source, dest, interaction, user, protocol, domain) : this;
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return !ActorMessageUtils.equals(this.dest, dest) ? new PodActorMessage<T, U>(null, tag, source, dest, interaction, user, protocol, domain) : this;
	}
}
