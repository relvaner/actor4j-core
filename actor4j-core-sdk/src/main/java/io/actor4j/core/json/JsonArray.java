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

import java.util.List;

import io.actor4j.core.utils.Shareable;

public interface JsonArray extends Shareable {
	public Object getValue(int pos);
	public String getString(int pos);
	public Integer getInteger(int pos);
	public Long getLong(int pos);
	public Double getDouble(int pos);
	public Boolean getBoolean(int pos);
	public JsonObject getJsonObject(int pos);
	public JsonArray getJsonArray(int pos);
	
	public JsonArray add(Object value);
	public JsonArray add(int pos, Object value);
	public JsonArray addAll(JsonArray array);
	
	public JsonArray set(int pos, Object value);
	public boolean contains(Object value);
	public Object remove(int pos);
	
	public int size();
	public boolean isEmpty();
	
	public List<Object> getList();
	public JsonArray clear();
	
	public String encode();
	public String encodePrettily();
}
