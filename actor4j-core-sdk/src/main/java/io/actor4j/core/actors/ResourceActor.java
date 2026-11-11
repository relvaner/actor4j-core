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

/*
 * stateful (default): messages are processed one after another (like an actor), may keep state, become; see StatefulResourceActor.
 * stateless: the same instance may run on several threads at once and in any order (default
 * and classic runtime; loom runs it sequentially): no fields, no become, no persistence; see StatelessResourceActor.
 * For parallel I/O prefer several stateful resource actors (e.g. a group behind one alias).
 */
public abstract class ResourceActor extends Actor {
	protected final boolean stateful;
	protected final boolean bulk;
	
	public ResourceActor() {
		this(null, true, false);
	}
	
	public ResourceActor(String name) {
		this(name, true, false);
	}
	
	@Deprecated // use StatefulResourceActor or StatelessResourceActor
	public ResourceActor(boolean stateful) {
		this(null, stateful, false);
	}
	
	@Deprecated // use StatefulResourceActor or StatelessResourceActor
	public ResourceActor(String name, boolean stateful) {
		this(name, stateful, false);
	}
	
	@Deprecated // use StatefulResourceActor or StatelessResourceActor
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
