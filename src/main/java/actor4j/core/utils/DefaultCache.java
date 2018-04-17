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

import java.util.HashMap;
import java.util.Map;

public class DefaultCache<K, E> implements Cache<K, E> {
	protected Map<K, E> map;
	
	public DefaultCache() {
		map = new HashMap<>();
	}
	
	@Override
	public E get(K key) {
		return map.get(key);
	}
	
	@Override
	public E put(K key, E value) {
		return map.put(key, value);
	}
	
	public void remove(K key) {
		map.remove(key);
	}
	
	public void clear() {
		map.clear();
	}
	
	protected void resize() {
		// empty
	}
	
	@Override
	public void gc(long maxTime) {
		// empty
	}

	@Override
	public String toString() {
		return "DefaultCache [map=" + map + "]";
	}
}
