/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.analyze;

import actor4j.core.ActorSystem;

public class ActorAnalyzer extends ActorSystem {

	public ActorAnalyzer(ActorAnalyzerThread analyzerThread) {
		super("actor4j-analyzer", AnalyzerActorSystemImpl.class);
		
		((AnalyzerActorSystemImpl)system).analyze(analyzerThread);
	}
}
