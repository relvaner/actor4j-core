/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core;

public class ActorServiceNode {
	protected final String name;
	protected final String uri;

	public ActorServiceNode(String name, String uri) {
		super();
		this.name = name;
		this.uri  = uri;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}
}
