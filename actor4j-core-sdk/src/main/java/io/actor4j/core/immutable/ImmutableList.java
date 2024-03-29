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
import java.util.List;

public class ImmutableList<T> implements ImmutableCollection<List<T>> {
	protected final List<T> list;
	
	public ImmutableList() {
		super();
		
		this.list = Collections.emptyList();
	}
	
	public ImmutableList(List<T> list) {
		super();
		
		this.list = Collections.unmodifiableList(list);
	}

	@Override
	public List<T> get() {
		return list;
	}
	
	public static <T> ImmutableList<T> of() {
		return new ImmutableList<>();
	}
	
	public static <T> ImmutableList<T> of(List<T> list) {
		return new ImmutableList<>(list);
	}

	@Override
	public String toString() {
		return "ImmutableList [list=" + list + "]";
	}
}
