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
package io.actor4j.core.actors;

public abstract class ResourceActor extends Actor {
	protected final boolean stateful;
	protected final boolean bulk;
	
	public ResourceActor() {
		this(null, false, false);
	}
	
	public ResourceActor(String name) {
		this(name, false, false);
	}
	
	public ResourceActor(boolean stateful) {
		this(null, stateful, false);
	}
	
	public ResourceActor(String name, boolean stateful) {
		this(name, stateful, false);
	}
	
	public ResourceActor(String name, boolean stateful, boolean bulk) {
		super(name);
		this.stateful = stateful;
		this.bulk = bulk;
	}
	
	public boolean isStateful() {
		return stateful;
	}

	public boolean isBulk() {
		return bulk;
	}

	public void before() {
		// empty
	}
	
	public void after() {
		// empty
	}
}
