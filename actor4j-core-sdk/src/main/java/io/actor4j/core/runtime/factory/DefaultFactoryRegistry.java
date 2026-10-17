/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFactoryRegistry<K> implements FactoryRegistry<K> {
	protected final Map<K, Factory<?>> map;
	
	public DefaultFactoryRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	@Override
	public void register(K key, Factory<?> factory) {
		map.put(key, factory);
	}
	
	@Override
	public Factory<?> getFactory(K key) {
		return map.get(key);
	}
	
	@Override
	public Object create(K key) {
		Object result = null;
		
		Factory<?> factory = map.get(key);
		if (factory!=null) {
			result = factory.create();
		}
		
		return result;
	}
	
	@Override
	public void unregister(K key) {
		map.remove(key);
	}
	
	public static <K> FactoryRegistry<K> create() {
		return new DefaultFactoryRegistry<>();
	}
}
