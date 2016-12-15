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
	protected List<ActorVerficationEdgeTuple> tuples;
	
	public ActorVerificationEdge(Set<Integer> events, List<ActorVerficationEdgeTuple> tuples) {
		super();
		this.events = events;
		this.tuples = tuples;
	}

	public Set<Integer> getEvents() {
		return events;
	}

	public List<ActorVerficationEdgeTuple> getTuples() {
		return tuples;
	}
	
	@Override
	protected String getSource() {
		return super.getSource().toString();
	}
	
	@Override
	protected String getTarget() {
		return super.getTarget().toString();
	}

	@Override
	public String toString() {
		return "ActorVerificationEdge [events=" + events + ", tuples=" + tuples + ", source=" + getSource() + ", target="
				+ getTarget() + "]";
	}
}
