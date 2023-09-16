/*
 * Copyright (c) 2015-2023, David A. Bauer. All rights reserved.
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
package io.actor4j.core.utils;

import java.util.HashMap;
import java.util.Map;

public class CacheAsMap<K, V> implements Cache<K, V> {
	protected final Map<K, V> map;
	
	public CacheAsMap() {
		map = new HashMap<>();
	}
	
	public Map<K, V> getMap() {
		return map;
	}

	@Override
	public V get(K key) {
		return map.get(key);
	}
	
	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}
	
	@Override
	public void remove(K key) {
		map.remove(key);
	}
	
	@Override
	public void clear() {
		map.clear();
	}
	
	@Override
	public void evict(long duration) {
		// empty
	}

	@Override
	public String toString() {
		return "CacheAsMap [map=" + map + "]";
	}
}
