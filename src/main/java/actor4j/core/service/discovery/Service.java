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
package actor4j.core.service.discovery;

import java.util.List;
import java.util.UUID;

import actor4j.core.utils.Shareable;

public class Service implements Shareable {
	protected final UUID id;
	protected final String name; // or for nodeName
	protected final String uri;  // or for path
	protected final List<String> topics;
	protected final String version;
	protected final String description;
	
	public Service(String name, String uri, List<String> topics, String version, String description) {
		super();
		id = UUID.randomUUID();
		this.name = name;
		this.uri = uri;
		this.topics = topics;
		this.version = version;
		this.description = description;
	}
	
	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public List<String> getTopics() {
		return topics;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isPath() {
		return uri!=null ? uri.indexOf("http")==-1 || uri.indexOf("https")==-1 : false;
	}
}
