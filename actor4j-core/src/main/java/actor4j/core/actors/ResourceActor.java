/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.actors;

public abstract class ResourceActor extends Actor {
	public ResourceActor() {
		super();
	}
	
	public ResourceActor(String name) {
		super(name);
	}
	
	public void before() {
		// empty
	}
	
	public void after() {
		// empty
	}
}
