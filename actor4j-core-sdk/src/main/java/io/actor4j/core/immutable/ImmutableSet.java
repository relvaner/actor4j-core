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
package io.actor4j.core.immutable;

import java.util.Collections;
import java.util.Set;

public class ImmutableSet<T> implements ImmutableCollection<Set<T>> {
	protected final Set<T> set;
	
	public ImmutableSet() {
		super();
		
		this.set = Collections.emptySet();
	}
	
	public ImmutableSet(Set<T> set) {
		super();
		
		this.set = Collections.unmodifiableSet(set);
	}

	@Override
	public Set<T> get() {
		return set;
	}
	
	public static <T> ImmutableSet<T> of() {
		return new ImmutableSet<>();
	}
	
	public static <T> ImmutableSet<T> of(Set<T> set) {
		return new ImmutableSet<>(set);
	}

	@Override
	public String toString() {
		return "ImmutableSet [set=" + set + "]";
	}
}
