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
import java.util.Map;

public class ImmutableMap<K, V> implements ImmutableCollection<Map<K, V>> {
	protected final Map<K, V> map;
	
	public ImmutableMap() {
		super();
		
		this.map = Collections.emptyMap();
	}
	
	public ImmutableMap(Map<K, V> map) {
		super();
		
		this.map = Collections.unmodifiableMap(map);
	}

	@Override
	public Map<K, V> get() {
		return map;
	}
	
	public static <K, V> ImmutableMap<K, V> of() {
		return new ImmutableMap<>();
	}
	
	public static <K, V> ImmutableMap<K, V> of(Map<K, V> map) {
		return new ImmutableMap<>(map);
	}

	@Override
	public String toString() {
		return "ImmutableMap [map=" + map + "]";
	}
}
