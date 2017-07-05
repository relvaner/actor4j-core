/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class CacheLRU<K, E> implements Cache<K, E> {
	protected Map<K, E> map;
	protected Deque<K> lru;
	
	protected int size;
	
	public CacheLRU(int size) {
		map = new HashMap<>(size);
		lru = new ArrayDeque<>(size);
		
		this.size = size;
	}
	
	@Override
	public E get(K key) {
		E result = map.get(key);
		
		if (result!=null) {
			lru.remove(key);
			lru.addLast(key);
		}
		
		return result;
	}
	
	@Override
	public E put(K key, E value) {
		E result = map.put(key, value);
		
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
