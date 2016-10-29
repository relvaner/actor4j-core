/*
 * Copyright (c) 2015-2016, David A. Bauer
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
