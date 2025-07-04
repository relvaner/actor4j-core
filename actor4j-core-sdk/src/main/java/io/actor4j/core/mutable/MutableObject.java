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
package io.actor4j.core.mutable;

public final class MutableObject<T> {
	private T value;

	public MutableObject() {
		this(null);
	}

	public MutableObject(T value) {
		super();
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	@SuppressWarnings("unchecked")
	public T add(T delta) {
		return value = (T) switch (value) {
			case Integer i -> Integer.valueOf(i+(int)delta);
			case Double  d -> Double.valueOf(d+(double)delta);
			case Float   f -> Float.valueOf(f+(float)delta);
			case Long    l -> Long.valueOf(l+(long)delta);
			case null, default -> value;
		};
	}

	@SuppressWarnings("unchecked")
	public T increment() {
		return value = (T) switch (value) {
			case Integer i -> Integer.valueOf(i+1);
			case Double  d -> Double.valueOf(d+1);
			case Float   f -> Float.valueOf(f+1);
			case Long    l -> Long.valueOf(l+1);
			case null, default -> value;
		};
	}
	
	@SuppressWarnings("unchecked")
	public T decrement() {
		return value = (T) switch (value) {
			case Integer i -> Integer.valueOf(i-1);
			case Double  d -> Double.valueOf(d-1);
			case Float   f -> Float.valueOf(f-1);
			case Long    l -> Long.valueOf(l-1);
			case null, default -> value;
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableObject<?> other = (MutableObject<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MutableObject [value=" + value + "]";
	}
}
