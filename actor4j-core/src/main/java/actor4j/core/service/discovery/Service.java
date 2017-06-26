/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.service.discovery;

import java.util.List;
import java.util.UUID;

public class Service {
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
		return uri!=null ? uri.indexOf("http")==-1 : false;
	}
}
