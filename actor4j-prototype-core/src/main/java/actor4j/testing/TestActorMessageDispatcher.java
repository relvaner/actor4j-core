/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.testing;

import java.util.UUID;
import java.util.function.BiConsumer;

import actor4j.core.ActorSystemImpl;
import actor4j.core.DefaultActorMessageDispatcher;
import actor4j.core.messages.ActorMessage;

public class TestActorMessageDispatcher extends DefaultActorMessageDispatcher {

	public TestActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
	}
	
	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		redirect(message);
		super.post(message, source, alias);
	}
	
	@Override
	protected void postQueue(ActorMessage<?> message, BiConsumer<Long, ActorMessage<?>> biconsumer) {
		redirect(message);
		super.postQueue(message, biconsumer);
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		redirect(message);
		super.postOuter(message);
	}
	
	protected void redirect(ActorMessage<?> message) {
		if (message!=null && ((TestSystemImpl)system).testActorId!=null && message.dest!=((TestSystemImpl)system).testActorId)
			message.dest = ((TestSystemImpl)system).pseudoActorId;
	}
}
