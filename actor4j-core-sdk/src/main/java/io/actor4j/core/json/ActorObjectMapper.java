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

import java.util.Optional;
import java.util.ServiceLoader;

import io.actor4j.core.json.spi.ActorObjectMapperProvider;

public interface ActorObjectMapper {
	public static ActorObjectMapper create() {
		Optional<ActorObjectMapperProvider> provider = ServiceLoader.load(ActorObjectMapperProvider.class).stream().map(ServiceLoader.Provider::get).findFirst();
		
		return provider.isPresent() ? provider.get().create() : null;
	}
	
	public String mapFrom(Object obj);
	public <T> T mapTo(String json, Class<T> type);
}
