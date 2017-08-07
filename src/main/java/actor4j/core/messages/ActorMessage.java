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

import java.util.List;
import java.util.UUID;

import actor4j.core.utils.Copyable;

public class ActorMessage<T> implements Copyable<ActorMessage<T>> {
	public T value;
	public int tag;
	public UUID source;
	public UUID dest;
	public boolean byRef;
	
	public ActorMessage(T value, int tag, UUID source, UUID dest) {
		super();
		
		this.value = value;
		this.tag = tag;
		this.source = source;
		this.dest = dest;
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

	public boolean valueAsBooolean() {
		return (boolean)value;
	}
	
	public int valueAsInt() {
		return (int)value;
	}
	
	public long valueAsLong() {
		return (long)value;
	}
	
	public double valueAsDouble() {
		return (double)value;
	}
	
	public String valueAsString() {
		return (String)value;
	}
	
	public List<?> valueAsList() {
		return (List<?>)value;
	}
	
	public UUID valueAsUUID() {
		return (UUID)value;
	}
	
	protected ActorMessage<T> weakCopy() {
		return new ActorMessage<T>(value, tag, source, dest);
	}
	
	@SuppressWarnings("unchecked")
	public ActorMessage<T> copy() {
		if (value!=null && !byRef && value instanceof Copyable)
			return new ActorMessage<T>(((Copyable<T>)value).copy(), tag, source, dest);	
		else /* if (value instanceof Shareable) */
			return new ActorMessage<T>(value, tag, source, dest);
		/* else throw with Error */
	}

	@Override
	public String toString() {
		return "ActorMessage [value=" + value + ", tag=" + tag + ", source=" + source + ", dest=" + dest + ", byRef="
				+ byRef + "]";
	}
}
