/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
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
package io.actor4j.core.json;

import java.util.Map;
import java.util.Set;

import io.actor4j.core.utils.Shareable;

public interface ActorJsonObject extends Shareable {
	public ActorJsonObject mapFrom(Object obj);
	
	public Object getValue(String key);
	public String getString(String key);
	public Integer getInteger(String key);
	public Long getLong(String key);
	public Double getDouble(String key);
	public Boolean getBoolean(String key);
	public ActorJsonObject getJsonObject(String key);
	public ActorJsonArray getJsonArray(String key);
	
	public boolean containsKey(String key);
	public Set<String> fieldNames();
	
	public ActorJsonObject put(String key, Object value);
	public Object remove(String key);
	
	public ActorJsonObject mergeIn(ActorJsonObject other);
	
	public String encode();
	public String encodePrettily();
	
	public Map<String,Object> getMap();
	
	public int size();
	public ActorJsonObject clear();
	public boolean isEmpty();
}
