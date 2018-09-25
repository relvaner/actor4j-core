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
package actor4j.core.messages;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import actor4j.core.utils.Copyable;
import actor4j.core.utils.Shareable;

public class FutureActorMessage<T> extends ActorMessage<T> {
	public final CompletableFuture<T> future;

	public FutureActorMessage(CompletableFuture<T> future, T value, int tag, UUID source, UUID dest) {
		super(value, tag, source, dest);
		this.future = future;
	}

	public FutureActorMessage(CompletableFuture<T> future, T value, Enum<?> tag, UUID source, UUID dest) {
		this(future, value, tag.ordinal(), source, dest);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (isSupportedType(value.getClass()) || value instanceof Shareable)
				return new FutureActorMessage<T>(future, value, tag, source, dest);
			else if (value instanceof Copyable)
				return new FutureActorMessage<T>(future, ((Copyable<T>)value).copy(), tag, source, dest);
			else if (value instanceof Exception)
				return new FutureActorMessage<T>(future, value, tag, source, dest);
			else
				throw new IllegalArgumentException();
		}
		else
			return new FutureActorMessage<T>(future, null, tag, source, dest);
	}
}
