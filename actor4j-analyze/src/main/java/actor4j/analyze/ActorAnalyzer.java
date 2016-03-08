/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.analyze;

import actor4j.core.ActorSystem;

public class ActorAnalyzer extends ActorSystem {

	public ActorAnalyzer(ActorAnalyzerThread analyzerThread) {
		this(null, analyzerThread);
	}

	public ActorAnalyzer(String name, ActorAnalyzerThread analyzerThread) {
		super(name, AnalyzerActorSystemImpl.class);
		
		((AnalyzerActorSystemImpl)system).analyze(analyzerThread);
	}
}
