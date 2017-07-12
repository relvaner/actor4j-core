/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.analyzer;

import java.util.UUID;
import java.util.function.BiConsumer;

import actor4j.core.ActorSystemImpl;
import actor4j.core.DefaultActorMessageDispatcher;
import actor4j.core.messages.ActorMessage;

public class AnalyzerActorMessageDispatcher extends DefaultActorMessageDispatcher {
	public AnalyzerActorMessageDispatcher(ActorSystemImpl system) {
		super(system);
	}

	@Override
	public void post(ActorMessage<?> message, UUID source, String alias) {
		if (alias!=null) {
			UUID dest = system.getAliases().get(alias);
			if (dest!=null)
				message.dest = dest;
		}
		analyze(message);
		super.post(message, source, alias);
	}
	
	@Override
	protected void postQueue(ActorMessage<?> message, BiConsumer<Long, ActorMessage<?>> biconsumer) {
		analyze(message);
		super.postQueue(message, biconsumer);
	}
	
	@Override
	public void postOuter(ActorMessage<?> message) {
		analyze(message);
		super.postOuter(message);
	}
	
	protected void analyze(ActorMessage<?> message) {
		if (message!=null && ((AnalyzerActorSystemImpl)system).getAnalyzeMode().get())
				((AnalyzerActorSystemImpl)system).getAnalyzerThread().getOuterQueue().offer(message.copy());
	}
}
