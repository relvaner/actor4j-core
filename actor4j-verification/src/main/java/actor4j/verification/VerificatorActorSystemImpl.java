/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import java.util.function.Consumer;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.DefaultActorSystemImpl;

public class VerificatorActorSystemImpl extends DefaultActorSystemImpl {
	
	public VerificatorActorSystemImpl(ActorSystem wrapper) {
		super(wrapper);
	}

	public VerificatorActorSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
	}
	
	public void verify(Consumer<ActorVerificationSM> consumer) {
		if (consumer!=null)
			for (ActorCell cell : cells.values())
				if (cell.getActor() instanceof ActorVerification)
					consumer.accept(((ActorVerification)cell.getActor()).verify());		
	}
	
	public void verifyAll(Consumer<ActorVerificationSM> consumer, Consumer<DirectedGraph<String, ActorVerificationEdge>> consumerAll) {
		DirectedGraph<String, ActorVerificationEdge> graph = new DefaultDirectedGraph<>(ActorVerificationEdge.class);
		verify((sm) -> {
			if (consumer!=null)
				consumer.accept(sm);
			Graphs.addGraph(graph, sm.getGraph());
		});
		
		if (consumerAll!=null)
			consumerAll.accept(graph);
	}
}
