/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods.actors;

import java.util.UUID;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorGroupMember;
import io.actor4j.core.pods.PodContext;

public abstract class PodChildActor extends Actor implements ActorGroupMember {
	protected UUID groupId;
	protected PodContext context;

	public PodChildActor(UUID groupId, PodContext context) {
		super();
		this.groupId = groupId;
		this.context = context;
	}

	@Override
	public UUID getGroupId() {
		return groupId;
	}
	
	@Override
	public void setAlias(String alias) {
		setAlias(alias, true);
	}
	
	public void setAlias(String alias, boolean absolute) {
		if (alias!=null && !alias.isEmpty())
			if (absolute)
				super.setAlias(alias+groupId);
			else
				super.setAlias(alias);
	}
	
	public String getAbsoluteAlias(String alias) {
		return alias+groupId;
	}
	
	public <T> T getPodDatabase() {
		return getSystem().getPodDatabase();
	}
}
