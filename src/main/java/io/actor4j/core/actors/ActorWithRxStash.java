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
package io.actor4j.core.actors;

import java.util.LinkedList;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorMessageFlowable;
import io.reactivex.Flowable;

public abstract class ActorWithRxStash extends Actor {
	protected Flowable<ActorMessage<?>> rxStash;
	
	public ActorWithRxStash() {
		this(null);
	}
	
	public ActorWithRxStash(String name) {
		super(name);
		
		stash   = new LinkedList<>();
		rxStash = ActorMessageFlowable.getMessages(stash);
	}
	
	public ActorMessage<?> unstash() {
		return stash.poll();
	}
}
