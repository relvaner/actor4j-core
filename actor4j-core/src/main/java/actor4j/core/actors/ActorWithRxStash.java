/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.actors;

import java.util.LinkedList;

import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorMessageObservable;
import rx.Observable;

public abstract class ActorWithRxStash extends Actor {
	protected Observable<ActorMessage<?>> rxStash;
	
	public ActorWithRxStash() {
		this(null);
	}
	
	public ActorWithRxStash(String name) {
		super(name);
		
		stash   = new LinkedList<>();
		rxStash = ActorMessageObservable.getMessages(stash);
	}
	
	public ActorMessage<?> unstash() {
		return stash.poll();
	}
}
