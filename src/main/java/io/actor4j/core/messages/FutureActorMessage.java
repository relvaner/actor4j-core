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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.actor4j.core.utils.Copyable;
import io.actor4j.core.utils.Shareable;

public class FutureActorMessage<T> extends ActorMessage<T> {
	public final CompletableFuture<T> future;
	
	public FutureActorMessage(CompletableFuture<T> future, T value, int tag, UUID source, UUID dest, UUID interaction, String protocol, String domain) {
		super(value, tag, source, dest, interaction, protocol, domain);
		this.future = future;
	}

	public FutureActorMessage(CompletableFuture<T> future, T value, int tag, UUID source, UUID dest) {
		this(future, value, tag, source, dest, null, null, null);
	}

	public FutureActorMessage(CompletableFuture<T> future, T value, Enum<?> tag, UUID source, UUID dest) {
		this(future, value, tag.ordinal(), source, dest);
	}
	
	@Override
	protected ActorMessage<T> weakCopy() {
		return new FutureActorMessage<T>(future, value, tag, source, dest, interaction, protocol, domain);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (isSupportedType(value.getClass()) || value instanceof Shareable)
				return new FutureActorMessage<T>(future, value, tag, source, dest, interaction, protocol, domain);
			else if (value instanceof Copyable)
				return new FutureActorMessage<T>(future, ((Copyable<T>)value).copy(), tag, source, dest, interaction, protocol, domain);
			else if (value instanceof Exception)
				return new FutureActorMessage<T>(future, value, tag, source, dest, interaction, protocol, domain);
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return new FutureActorMessage<T>(future, null, tag, source, dest, interaction, protocol, domain);
	}

	@Override
	public String toString() {
		return "FutureActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest
				+ ", interaction=" + interaction + ", protocol=" + protocol + ", domain=" + domain + "]";
	}
}
