/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import org.jgrapht.DirectedGraph;

import actor4j.core.ActorSystem;
import actor4j.function.Consumer;

public class ActorVerificator extends ActorSystem {
	
	public ActorVerificator() {
		super("actor4j-verification", VerificatorActorSystemImpl.class);
	}
	
	public void verify(Consumer<ActorVerificationSM> consumer) {
		((VerificatorActorSystemImpl)system).verify(consumer);
	}
	
	public void verifyAll(Consumer<ActorVerificationSM> consumer, Consumer<DirectedGraph<String, ActorVerificationEdge>> consumerAll) {
		((VerificatorActorSystemImpl)system).verifyAll(consumer, consumerAll);
	}
}
