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
package cloud.actor4j.core.actors;

public abstract class ResourceActor extends Actor {
	protected final boolean bulk;
	
	public ResourceActor() {
		this(null, false);
	}
	
	public ResourceActor(String name) {
		this(name, false);
	}
	
	public ResourceActor(boolean bulk) {
		this(null, bulk);
	}
	
	public ResourceActor(String name, boolean bulk) {
		super(name);
		this.bulk = bulk;
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
