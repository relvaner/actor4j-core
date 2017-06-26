/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core;

import java.util.UUID;

import actor4j.core.messages.ActorMessage;

/* currently not used */
public class ActorRef {
	protected ActorSystem system;
	protected UUID ref;
	
	public ActorRef(ActorSystem system) {
		super();
		this.system = system;
		
		ref = UUID.randomUUID();
	}
	
	public <T> ActorRef tell(T value, int tag, ActorRef dest) {
		system.send(new ActorMessage<T>(value, tag, ref, dest.ref));
		
		return this;
	}
}
