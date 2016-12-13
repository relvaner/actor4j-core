/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

public class ActorVerificationEdge extends DefaultEdge {
	protected static final long serialVersionUID = -3962581299999193897L;
	
	protected Set<Integer> events;
	protected List<ActorVerficationMessageTuple> tuples;
	
	public ActorVerificationEdge(Set<Integer> events, List<ActorVerficationMessageTuple> tuples) {
		super();
		this.events = events;
		this.tuples = tuples;
	}

	public Set<Integer> getEvents() {
		return events;
	}

	public List<ActorVerficationMessageTuple> getTuples() {
		return tuples;
	}
}
