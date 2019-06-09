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
package cloud.actor4j.core.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class ActorGroupList extends LinkedList<UUID> implements ActorGroup {
	protected static final long serialVersionUID = 8641920411195875484L;
	
	protected final UUID id;
	
	public ActorGroupList() {
		super();
		
		id = UUID.randomUUID();
	}

	public ActorGroupList(Collection<UUID> c) {
		super(c);
		
		id = UUID.randomUUID();
	}

	@Override
	public UUID getId() {
		return id;
	}
}
