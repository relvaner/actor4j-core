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
package io.actor4j.core.runtime.extended.di;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.actor4j.core.runtime.di.DIContainer;
import io.actor4j.core.runtime.di.FactoryInjector;
import io.actor4j.core.utils.Utils;

public class DefaultDIContainer<K> implements DIContainer<K> {
	protected Map<K, DIMapEntry> diMap;
	
	public DefaultDIContainer() {
		diMap = new ConcurrentHashMap<>();
	}

	public void registerConstructorInjector(K key, Class<?> base, Object... params) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		entry.setBase(base);
		entry.getConstructorInjector().setParams(params);
		
		diMap.put(key, entry);
	}
	
	public void registerFactoryInjector(K key, FactoryInjector<?> factoryInjector) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		entry.setFactoryInjector(factoryInjector);
		
		diMap.put(key, entry);
	}
	
	@Override
	public void register(K key, FactoryInjector<?> factoryInjector) {
		registerFactoryInjector(key, factoryInjector);
	}
	
	protected Object buildInstance(Class<?> base, Object[] params) throws Exception {
		Object result = null;
		
		Class<?>[] paramsTypes = new Class<?>[params.length];
		for (int i=0; i<params.length; i++)
			paramsTypes[i] = params[i].getClass();
		
		Constructor<?> foundConstructor = null;
		for (Constructor<?> constructor : base.getDeclaredConstructors()) {
			Class<?>[] types = constructor.getParameterTypes();
			if (types.length!=paramsTypes.length)
				continue;
			boolean found = true;
			for (int i=0; i<types.length; i++) {
				Class<?> type = types[i];
				if (type.isPrimitive()) {
					if (!Utils.getPrimitiveWrapper(type).equals(paramsTypes[i]))
						found = false;
				}
				else if (type.isInterface()) {
					if (!type.isAssignableFrom(paramsTypes[i]))
						found = false;
				}
				else if (!type.equals(paramsTypes[i]))
					found = false;

				if (!found)
					break;
			}
			
			if (found) {
				foundConstructor = constructor;
				break;
			}
		}
			
		if (foundConstructor!=null)
			result = foundConstructor.newInstance(params);
		else
			throw new NoSuchMethodException();
		
		return result;
	}
	
	public Object getInstance(K key) throws Exception {
		Object result = null;
		
		DIMapEntry entry = diMap.get(key);
		if (entry!=null) {
			if (entry.getFactoryInjector()!=null)
				result = entry.getFactoryInjector().create();
			else {
				if (entry.getConstructorInjector().getParams()!=null)
					result = buildInstance(entry.getBase(), entry.getConstructorInjector().getParams());
				else
					// https://docs.oracle.com/javase/9/docs/api/java/lang/Class.html#newInstance--
					result = entry.getBase().getDeclaredConstructor().newInstance();
			}
		}
		
		return result;
	}
	
	@Override
	public void unregister(K key) {
		diMap.remove(key);
	}
	
	public static <K> DIContainer<K> create() {
		return new DefaultDIContainer<>();
	}
}
