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
package io.actor4j.core.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CacheLRU<K, V> implements Cache<K, V> {
	protected final Map<K, V> map;
	protected final int size;
	
	public CacheLRU(int size) {
		this.size = size;
		
		map = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size()>CacheLRU.this.size;
			}
		};
	}
	
	public Map<K, V> getMap() {
		return map;
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	@Override
	public V get(K key) {
		return map.get(key);
	}
	
	@Override
	public Map<K, V> get(List<K> keys) {
		Map<K, V> result = new HashMap<>();
		for (K key : keys) {
			V value = map.get(key);
			if (value!=null)
				result.put(key, value);
		}
		
		return result;
	}
	
	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}
	
	@Override
	public void put(Map<K, V> entries) {
		entries.entrySet()
			.stream()
			.forEach(entry -> put(entry.getKey(), entry.getValue()));
	}
	
	@Override
	public boolean compareAndSet(K key, V expectedValue, V newValue) {
		boolean result = false;
		
		V value = map.get(key);
		if (value!=null && value.equals(expectedValue)) {
			put(key, newValue);
			result = true;
		}

		return result;
	}
	
	@Override
	public void remove(K key) {
		map.remove(key);
	}
	
	@Override
	public void remove(List<K> keys) {
		keys.stream().forEach(key -> remove(key));
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
	public void close() {
		// empty
	}

	@Override
	public String toString() {
		return "CacheLRU [map=" + map  + ", size=" + size + "]";
	}
}
