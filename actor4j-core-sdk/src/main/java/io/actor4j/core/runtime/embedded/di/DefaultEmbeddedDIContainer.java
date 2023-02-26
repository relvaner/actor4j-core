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
package io.actor4j.core.runtime.embedded.di;

import java.util.HashMap;
import java.util.Map;

import io.actor4j.core.runtime.di.FactoryInjector;

public class DefaultEmbeddedDIContainer<K> implements EmbeddedDIContainer<K> {
	protected final Map<K, FactoryInjector<?>> diMap;
	
	public DefaultEmbeddedDIContainer() {
		diMap = new HashMap<>();
	}
	
	@Override
	public void register(K key, FactoryInjector<?> factoryInjector) {
		diMap.put(key, factoryInjector);
	}
	
	public FactoryInjector<?> getFactory(K key) {
		return diMap.get(key);
	}
	
	@Override
	public Object getInstance(K key) throws Exception {
		Object result = null;
		
		FactoryInjector<?> factoryInjector = diMap.get(key);
		if (factoryInjector!=null) {
			result = factoryInjector.create();
		}
		
		return result;
	}
	
	@Override
	public void unregister(K key) {
		diMap.remove(key);
	}
	
	public static <K> EmbeddedDIContainer<K> create() {
		return new DefaultEmbeddedDIContainer<>();
	}
}
