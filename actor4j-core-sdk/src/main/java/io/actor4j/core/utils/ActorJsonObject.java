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

import java.util.Iterator;
import java.util.Map.Entry;

public interface ActorJsonObject extends Shareable {
	public ActorJsonObject mapFrom(Object obj);
	public <T> T mapTo(Class<T> type);
	
	public Object getValue(String key);
	public String getString(String key);
	public int getInteger(String key);
	public long getLong(String key);
	public double getDouble(String key);
	public boolean getBoolean(String key);
	public ActorJsonObject getJsonObject(String key);
	public ActorJsonArray getJsonArray(String key);
	
	public boolean containsKey(String key);
	
	public ActorJsonObject put(String key, Object value);
	public Object remove(String key);
	
	public ActorJsonObject mergeIn(ActorJsonObject other);
	
	public Iterator<Entry<String,Object>> iterator();
	
	public String encode();
	public String encodePrettily();
	
	public int size();
	public ActorJsonObject clear();
	public boolean isEmpty();
}
