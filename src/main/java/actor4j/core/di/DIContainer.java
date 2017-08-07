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
package actor4j.core.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.di.DIConfiguration;
import actor4j.core.di.DIContainer;
import actor4j.core.di.DIMapEntry;
import actor4j.core.di.FactoryInjector;
import actor4j.core.di.InjectorParam;
import actor4j.core.di.MethodInjector;
import actor4j.core.di.SetterInjector;
import actor4j.core.utils.Utils;

public class DIContainer<K> {
	protected Map<K, DIMapEntry> diMap;
	
	public DIContainer() {
		diMap = new ConcurrentHashMap<>();
	}
	
	public void registerConstructorInjector(K key, Class<?> base, Class<?> type, Class<?> injector, Object... args) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		entry.setBase(base);
		entry.getConstructorInjector().getParams().add(new InjectorParam(type, injector, args));
		
		diMap.put(key, entry);
	}
	
	public void registerConstructorInjector(K key, Class<?> base, Class<?> injector, Object... args) {
		registerConstructorInjector(key, base, null, injector, args);
	}
	
	public void registerConstructorInjector(K key, Class<?> base, InjectorParam... params) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		entry.setBase(base);
		entry.getConstructorInjector().setParams(Arrays.asList(params));
		
		diMap.put(key, entry);
	}
	
	public void registerSetterInjector(K key, Class<?> base, String valueName, Class<?> type, Class<?> injector, Object... args) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		SetterInjector setterInjector = new SetterInjector();
		setterInjector.setValueName(valueName);
		setterInjector.setParam(new InjectorParam(type, injector, args));
		entry.setBase(base);
		entry.getSetterInjectorList().add(setterInjector);
		
		diMap.put(key, entry);
	}
	
	public void registerSetterInjector(K key, Class<?> base, String valueName, Class<?> injector, Object... args) {
		registerSetterInjector(key, base, valueName, null, injector, args);
	}
	
	public void registerMethodInjector(K key, Class<?> base, String name, Class<?> type, Class<?> injector, Object... args) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		MethodInjector methodInjector = new MethodInjector();
		methodInjector.setName(name);
		methodInjector.getParams().add(new InjectorParam(type, injector, args));
		entry.setBase(base);
		entry.getMethodInjectorList().add(methodInjector);
		
		diMap.put(key, entry);
	}
	
	public void registerMethodInjector(K key, Class<?> base, String name, Class<?> injector, Object... args) {
		registerMethodInjector(key, base, name, null, injector, args);
	}
	
	public void registerMethodInjector(K key, Class<?> base, String name, InjectorParam... params) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		entry.setBase(base);
		MethodInjector methodInjector = new MethodInjector();
		methodInjector.setName(name);
		methodInjector.setParams(Arrays.asList(params));
		entry.getMethodInjectorList().add(methodInjector);
		
		diMap.put(key, entry);
	}
	
	public void registerFactoryInjector(K key, FactoryInjector<?> factoryInjector) {
		DIMapEntry entry = diMap.get(key);
		if (entry==null)
			entry = new DIMapEntry();
		entry.setFactoryInjector(factoryInjector);
		
		diMap.put(key, entry);
	}
	
	@SuppressWarnings("unchecked")
	protected void generate(List<InjectorParam> params, Class<?>[] types, Object[] injectorObjects) throws Exception {
		for (int i=0; i<params.size(); i++) {
			InjectorParam param = params.get(i);
			
			if (param.ref!=null) {
				injectorObjects[i] = getInstance((K)param.ref);
				types[i] = injectorObjects[i].getClass();
				continue;
			} else if (param.obj!=null) {
				injectorObjects[i] = param.obj;
				types[i] = injectorObjects[i].getClass();
				continue;
			}
			
			if (param.type!=null)
				types[i] = param.type;
			else
				types[i] = param.injector;
			
			Class<?>[] injectorTypes = new Class<?>[param.args.length];
			for (int j=0; j<param.args.length; j++) {
				injectorTypes[j] = param.args[j].getClass();
			}
			
			if (param.args.length==1 && Utils.isWrapperType(param.args[0].getClass()))
				injectorObjects[i] = param.args[0];
			else {
				Constructor<?> c1 = param.injector.getConstructor(injectorTypes);
				injectorObjects[i] = c1.newInstance(param.args);
			}
		}
	}
	
	protected Object buildInstance(Class<?> base, List<InjectorParam> params) throws Exception {
		Object result = null;
		
		Class<?>[] types = new Class<?>[params.size()];
		Object[] injectorObjects = new Object[params.size()];
		generate(params, types, injectorObjects);
		
		Constructor<?> c2 = base.getConstructor(types);
		result = c2.newInstance(injectorObjects);
		
		return result;
	}
	
	protected void callSetters(Object instance, Class<?> base, List<SetterInjector> list) throws Exception {
		for (int i=0; i<list.size(); i++) {
			SetterInjector setterInjector = list.get(i);
			
			Class<?>[] types = new Class<?>[1];
			Object[] injectorObjects = new Object[1];
			List<InjectorParam> params = new ArrayList<>(1);
			params.add(setterInjector.getParam());
			generate(params, types, injectorObjects);
			
			Method m2 = base.getMethod("set"+setterInjector.getValueName().substring(0,1).toUpperCase()+setterInjector.getValueName().substring(1), types[0]);
			m2.setAccessible(true);
			m2.invoke(instance, injectorObjects[0]);
		}
	}
	
	protected void callMethods(Object instance, Class<?> base, List<MethodInjector> list) throws Exception {
		for (int i=0; i<list.size(); i++) {
			MethodInjector methodInjector = list.get(i);
			
			Class<?>[] types = new Class<?>[methodInjector.params.size()];
			Object[] injectorObjects = new Object[methodInjector.params.size()];
			generate(methodInjector.params, types, injectorObjects);
			
			Method m2 = base.getMethod(methodInjector.getName(), types);
			m2.setAccessible(true);
			m2.invoke(instance, injectorObjects);
		}
	}
	
	public Object getInstance(K key) throws Exception {
		Object result = null;
		
		DIMapEntry entry = diMap.get(key);
		if (entry!=null) {
			if (entry.getFactoryInjector()!=null)
				result = entry.getFactoryInjector().create();
			else {
				if (entry.getConstructorInjector().getParams().size()>0)
					result = buildInstance(entry.getBase(), entry.getConstructorInjector().getParams());
				else
					result = entry.getBase().newInstance();
			}
			
			if (entry.getSetterInjectorList().size()>0)
				callSetters(result, entry.getBase(), entry.getSetterInjectorList());
			
			if (entry.getMethodInjectorList().size()>0)
				callMethods(result, entry.getBase(), entry.getMethodInjectorList());
		}
		
		return result;
	}
	
	public DIMapEntry unregister(K key) {
		return diMap.remove(key);
	}
	
	public static <K> DIContainer<K> create() {
		return new DIContainer<>();
	}
	
	public static <K> DIContainer<K> create(DIConfiguration<K> configuration) {
		DIContainer<K> container = new DIContainer<>();
		configuration.configure(container);
		
		return container;
	}
}
