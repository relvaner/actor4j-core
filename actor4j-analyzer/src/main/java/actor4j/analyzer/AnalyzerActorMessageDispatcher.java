/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.analyzer;

import java.util.UUID;

import actor4j.core.ActorSystemImpl;
import actor4j.core.DefaultActorMessageDispatcher;
import actor4j.core.messages.ActorMessage;
import actor4j.function.BiConsumer;

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
