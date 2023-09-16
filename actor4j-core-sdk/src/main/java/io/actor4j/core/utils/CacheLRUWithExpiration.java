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
package io.actor4j.core.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class CacheLRUWithExpiration<K, V> implements Cache<K, V>  {
	protected static class Pair<V> {
		public V value;
		public long timestamp;
		
		public Pair(V value, long timestamp) {
			this.value = value;
			this.timestamp = timestamp;
		}
	}
	
	protected Map<K, Pair<V>> map;
	protected SortedMap<Long, K> lru;
	
	protected int size;
	
	public CacheLRUWithExpiration(int size) {
		map = new HashMap<>(size);
		lru = new TreeMap<>();
		
		this.size = size;
	}
		
	public Map<K, Pair<V>> getMap() {
		return map;
	}
	
	public SortedMap<Long, K> getLru() {
		return lru;
	}

	@Override
	public V get(K key) {
		V result = null;
		
		Pair<V> pair = map.get(key);
		if (pair!=null) {
			lru.remove(pair.timestamp);
			pair.timestamp = System.nanoTime();
			lru.put(pair.timestamp, key);
			result = pair.value;
		}
		
		return result;
	}
	
	@Override
	public V put(K key, V value) {
		V result = null;
		
		long timestamp = System.nanoTime();
		Pair<V> pair = map.put(key, new Pair<V>(value, timestamp));
		if (pair==null) {
			resize();
			lru.put(timestamp, key);
		}
		else {
			lru.remove(pair.timestamp);
			lru.put(timestamp, key);
			result = pair.value;
		}
		
		return result;
	}
	
	public void remove(K key) {
		Pair<V> pair = map.get(key);
		lru.remove(pair.timestamp);
		map.remove(key);
	}
	
	public void clear() {
		map.clear();
		lru.clear();
	}
	
	protected void resize() {
		if (map.size()>size) {
			long timestamp = lru.firstKey();
			map.remove(lru.get(timestamp));
			lru.remove(timestamp);
		}
	}
	
	@Override
	public void evict(long duration) {
		long currentTime = System.currentTimeMillis();
		
		Iterator<Entry<Long, K>> iterator = lru.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Long, K> entry = iterator.next();
			if (currentTime-entry.getKey()/1_000_000>duration) {
				map.remove(entry.getValue());
				iterator.remove();
			}
		}
	}

	@Override
	public String toString() {
		return "CacheLRUWithGC [map=" + map + ", lru=" + lru + ", size=" + size + "]";
	}
}