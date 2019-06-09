/*
 * tools4j - Java Library
 * Copyright (c) 2008-2017, David A. Bauer
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cloud.actor4j.core.di;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cloud.actor4j.core.di.DIContainer;
import cloud.actor4j.core.di.DIMapEntry;
import cloud.actor4j.core.di.FactoryInjector;

// Adapted for actor4j
public class DIContainer<K> {
	protected Map<K, DIMapEntry> diMap;
	
	public DIContainer() {
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
	
	protected Object buildInstance(Class<?> base, Object[] params) throws Exception {
		Object result = null;
		
		Class<?>[] types = new Class<?>[params.length];
		for (int i=0; i<params.length; i++)
			types[i] = params[i].getClass();
		
		Constructor<?> c2 = base.getConstructor(types);
		result = c2.newInstance(params);
		
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
					result = entry.getBase().newInstance();
			}
		}
		
		return result;
	}
	
	public DIMapEntry unregister(K key) {
		return diMap.remove(key);
	}
	
	public static <K> DIContainer<K> create() {
		return new DIContainer<>();
	}
}
