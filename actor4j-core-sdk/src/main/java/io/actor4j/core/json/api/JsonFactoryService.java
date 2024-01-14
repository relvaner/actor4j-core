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
package io.actor4j.core.json.api;

import java.util.List;

import io.actor4j.core.json.JsonArray;
import io.actor4j.core.json.JsonObject;

public interface JsonFactoryService {
	public JsonObject createJsonObject();
	public JsonObject createJsonObject(Object obj);
	public JsonObject createJsonObject(String json);
	
	public JsonArray createJsonArray();
	public JsonArray createJsonArray(Object obj);
	public JsonArray createJsonArray(List<?> list);
	public JsonArray createJsonArray(String json);
}
