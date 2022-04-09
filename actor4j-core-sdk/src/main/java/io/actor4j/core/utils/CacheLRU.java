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
package io.actor4j.core.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class CacheLRU<K, V> implements Cache<K, V> {
	protected Map<K, V> map;
	protected Deque<K> lru;
	
	protected int size;
	
	public CacheLRU(int size) {
		map = new HashMap<>(size);
		lru = new ArrayDeque<>(size);
		
		this.size = size;
	}
	
	public Map<K, V> getMap() {
		return map;
	}

	public Deque<K> getLru() {
		return lru;
	}

	@Override
	public V get(K key) {
		V result = map.get(key);
		
		if (result!=null) {
			lru.remove(key);
			lru.addLast(key);
		}
		
		return result;
	}
	
	@Override
	public V put(K key, V value) {
		V result = map.put(key, value);
		
		if (result==null) {
			resize();
			lru.addLast(key);
		}
		else {
			lru.remove(key);
			lru.addLast(key);
		}
		
		return result;
	}
	
	public void remove(K key) {
		map.remove(key);
		lru.remove(key);
	}
	
	public void clear() {
		map.clear();
		lru.clear();
	}
	
	protected void resize() {
		if (map.size()>size) {
			map.remove(lru.getFirst());
			lru.removeFirst();
		}
	}
	
	@Override
	public void gc(long maxTime) {
		// empty
	}

	@Override
	public String toString() {
		return "CacheLRU [map=" + map + ", lru=" + lru + ", size=" + size + "]";
	}
}
