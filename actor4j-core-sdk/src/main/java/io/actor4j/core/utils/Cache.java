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

import java.util.List;
import java.util.Map;

public interface Cache<K, V> {
	public boolean containsKey(K key);
	
	public V get(K key);
	public Map<K, V> get(List<K> keys);
	
	public V put(K key, V value);
	public void put(Map<K, V> entries);
	
	public void remove(K key);
	public void remove(List<K> keys);
	
	public void clear();

	public void evict(long duration);
	
	public void close();
}
