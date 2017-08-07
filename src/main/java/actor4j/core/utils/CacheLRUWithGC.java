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
package actor4j.core.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CacheLRUWithGC<K, E> implements Cache<K, E>  {
	protected Map<K, E> map;
	protected Deque<K> lru;
	protected Map<K, Long> timestampMap;
	
	protected int size;
	
	public CacheLRUWithGC(int size) {
		map = new HashMap<>(size);
		lru = new ArrayDeque<>(size);
		timestampMap = new HashMap<>(size); 
		
		this.size = size;
	}
	
	@Override
	public E get(K key) {
		E result = map.get(key);
		
		if (result!=null) {
			lru.remove(key);
			lru.addLast(key);
			timestampMap.put(key, System.currentTimeMillis());
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
		timestampMap.put(key, System.currentTimeMillis());
		
		return result;
	}
	
	public void remove(K key) {
		map.remove(key);
		timestampMap.remove(key);
		lru.remove(key);
	}
	
	public void clear() {
		map.clear();
		timestampMap.clear();
		lru.clear();
	}
	
	protected void resize() {
		if (map.size()>size) {
			map.remove(lru.getFirst());
			timestampMap.remove(lru.getFirst());
			lru.removeFirst();
		}
	}
	
	@Override
	public void gc(long maxTime) {
		long currentTime = System.currentTimeMillis();
		
		Iterator<K> iterator = lru.iterator();
		while (iterator.hasNext()) {
			K key = iterator.next();
			if (currentTime-timestampMap.get(key)>maxTime) {
				map.remove(key);
				iterator.remove();
				timestampMap.remove(key);
			}
		}
	}

	@Override
	public String toString() {
		return "CacheLRUWithGC [map=" + map + ", lru=" + lru + ", timestampMap=" + timestampMap + ", size=" + size
				+ "]";
	}
}
