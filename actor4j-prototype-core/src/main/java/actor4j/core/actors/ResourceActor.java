/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
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
