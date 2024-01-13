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
package io.actor4j.core.json;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import io.actor4j.core.utils.Shareable;

public interface ActorJsonArray extends Shareable {
	public Object getValue(int pos);
	public String getString(int pos);
	public int getInteger(int pos);
	public long getLong(int pos);
	public double getDouble(int pos);
	public boolean getBoolean(int pos);
	public ActorJsonObject getJsonObject(int pos);
	public ActorJsonArray getJsonArray(int pos);
	
	public ActorJsonArray add(Object value);
	public ActorJsonArray add(int pos, Object value);
	public ActorJsonArray addAll(ActorJsonArray array);
	
	public ActorJsonArray set(int pos, Object value);
	public boolean contains(Object value);
	public boolean remove(Object value);
	public Object remove(int pos);
	
	public int size();
	public boolean isEmpty();
	
	public List<Object> getList();
	public ActorJsonArray clear();
	public Iterator<Object> iterator();
	
	public String encode();
	public String encodePrettily();
	
	public Stream<Object> stream();
}
