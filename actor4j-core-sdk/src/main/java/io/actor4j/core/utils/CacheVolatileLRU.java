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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CacheVolatileLRU<K, V> implements Cache<K, V>  {
	protected static class Pair<V> {
		public final V value;
		public long timestamp;
		
		public Pair(V value, long timestamp) {
			this.value = value;
			this.timestamp = timestamp;
		}
	}
	
	protected final Map<K, Pair<V>> map;
	protected final int size;
	
	public CacheVolatileLRU(int size) {
		this.size = size;
		
		map = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, Pair<V>> eldest) {
				return size()>CacheVolatileLRU.this.size;
			}
		};
	}
		
	public Map<K, Pair<V>> getMap() {
		return map;
	}
	
	@Override
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	@Override
	public V get(K key) {
		V result = null;
		
		Pair<V> pair = map.get(key);
		if (pair!=null) {
			pair.timestamp = System.nanoTime();
			result = pair.value;
		}
		
		return result;
	}
	
	@Override
	public Map<K, V> get(List<K> keys) {
		Map<K, V> result = new HashMap<>();
		for (K key : keys) {
			V value = get(key);
			if (value!=null)
				result.put(key, value);
		}
		
		return result;
	}
	
	@Override
	public V put(K key, V value) {
		V result = null;
		
		long timestamp = System.nanoTime();
		Pair<V> pair = map.put(key, new Pair<V>(value, timestamp));
		if (pair!=null)
			result = pair.value;
		
		return result;
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
		
		Pair<V> pair = map.get(key);
		if (pair!=null && pair.value!=null && pair.value.equals(expectedValue)) {
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
		long currentTime = System.nanoTime();
		
		Iterator<Entry<K, Pair<V>>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<K, Pair<V>> entry = iterator.next();
			if ((currentTime-entry.getValue().timestamp)/1_000_000>duration)
				iterator.remove();
		}
	}
	
	@Override
	public void close() {
		// empty
	}

	@Override
	public String toString() {
		return "CacheVolatileLRU [map=" + map  + ", size=" + size + "]";
	}
}