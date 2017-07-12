/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.analyzer;

import actor4j.core.ActorSystem;

public class ActorAnalyzer extends ActorSystem {

	public ActorAnalyzer(ActorAnalyzerThread analyzerThread) {
		super("actor4j-analyzer", AnalyzerActorSystemImpl.class);
		
		((AnalyzerActorSystemImpl)system).analyze(analyzerThread);
	}
}
