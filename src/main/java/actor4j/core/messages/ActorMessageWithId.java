/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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

import actor4j.core.utils.Copyable;
import actor4j.core.utils.Shareable;

public class ActorMessageWithId<T> extends ActorMessage<T> {
	public final UUID id;

	public ActorMessageWithId(T value, int tag, UUID source, UUID dest) {
		super(value, tag, source, dest);
		
		id = UUID.randomUUID();
	}
	
	public ActorMessageWithId(T value, int tag, UUID source, UUID dest, UUID id) {
		super(value, tag, source, dest);
		
		this.id = id;
	}
	
	public UUID getId() {
		return id;
	}

	@Override
	protected ActorMessage<T> weakCopy() {
		return new ActorMessageWithId<T>(value, tag, source, dest, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ActorMessage<T> copy() {
		if (value!=null) { 
			if (isSupportedType(value.getClass()) || value instanceof Shareable)
				return new ActorMessageWithId<T>(value, tag, source, dest, id);
			else if (value instanceof Copyable)
				return new ActorMessageWithId<T>(((Copyable<T>)value).copy(), tag, source, dest, id);
			else if (value instanceof Exception)
				return new ActorMessageWithId<T>(value, tag, source, dest, id);
			else
				throw new IllegalArgumentException(value.getClass().getName());
		}
		else
			return new ActorMessageWithId<T>(null, tag, source, dest, id);
	}
}
