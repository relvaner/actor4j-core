/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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

import java.util.Objects;

public class MutableBoolean implements Mutable<Boolean>, Comparable<MutableBoolean> {
	private boolean value;
	
	public MutableBoolean(boolean value) {
		super();
		this.value = value;
	}

	@Override
	public Boolean get() {
		return value;
	}

	@Override
	public void set(Boolean value) {
		this.value = value;
	}
	
	public void set(boolean value) {
		this.value = value;
	}
	
	public boolean isTrue() {
		return value;
	}
	
	public boolean isFalse() {
		return !value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableBoolean other = (MutableBoolean) obj;
		return Objects.equals(value, other.value);
	}
	
	@Override
	public int compareTo(MutableBoolean o) {
		return Boolean.compare(value, o.get());
	}

	@Override
	public String toString() {
		return "MutableBoolean [value=" + value + "]";
	}
}
