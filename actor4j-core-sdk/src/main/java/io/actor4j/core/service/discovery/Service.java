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
package io.actor4j.core.service.discovery;

import java.util.List;
import java.util.UUID;

public record Service(UUID id, String name, UUID adddress, String alias, String path, List<String> topics, String version, String description) {
	public Service(String name, UUID adddress, String alias, String path, List<String> topics, String version, String description) {
		this(UUID.randomUUID(), name, adddress, alias, path, topics, version, description);
	}
	
	public Service(String name, UUID adddress, String alias, List<String> topics, String version, String description) {
		this(UUID.randomUUID(), name, adddress, alias, null, topics, version, description);
	}
	
	public Service(String name, String alias, String path, List<String> topics, String version, String description) {
		this(UUID.randomUUID(), name, null, alias, path, topics, version, description);
	}
	
	public Service(String name, String alias, List<String> topics, String version, String description) {
		this(UUID.randomUUID(), name, null, alias, null, topics, version, description);
	}
}
