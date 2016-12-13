/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import actor4j.core.actors.Actor;

public class ActorVerificationSM {
	protected DirectedGraph<String, ActorVerificationEdge> graph;
	protected Actor actor;
	protected String prefix;
	
	public ActorVerificationSM(Actor actor) {
		super();
		this.actor = actor;
		graph = new DefaultDirectedGraph<>(ActorVerificationEdge.class);
		prefix = (actor.getName()!=null ? actor.getName():actor.getId())+":"; 
	}
	
	public DirectedGraph<String, ActorVerificationEdge> getGraph() {
		return graph;
	}

	public boolean addStateMarker(String state) {
		return graph.addVertex(prefix+state);
	}
	
	protected boolean addTransition(String sourceState, String targetState, Set<Integer> events, List<ActorVerficationMessageTuple> tuples) {
		ActorVerificationEdge edge = graph.getEdge(prefix+sourceState, prefix+targetState);
		if (edge!=null) {
			if (events!=null)
				edge.events = events;
			if (tuples!=null)
				edge.tuples = tuples;
			return false;
		}
		else
			return graph.addEdge(prefix+sourceState, prefix+targetState, new ActorVerificationEdge(events, tuples));
	};
	
	protected boolean addTransition(String sourceState, String targetState, int event) {
		ActorVerificationEdge edge = graph.getEdge(prefix+sourceState, prefix+targetState);
		if (edge!=null) {
			if (edge.events!=null)
				edge.events.add(event);
			else
				edge.events = new HashSet<Integer>(Arrays.asList(event));
			return false;
		}
		else
			return graph.addEdge(prefix+sourceState, prefix+targetState, new ActorVerificationEdge(new HashSet<Integer>(Arrays.asList(event)), null));
	};
	
	protected boolean addTransition(String sourceState, String targetState, ActorVerficationMessageTuple tuple) {
		ActorVerificationEdge edge = graph.getEdge(prefix+sourceState, prefix+targetState);
		if (edge!=null) {
			if (edge.tuples!=null)
				edge.tuples.add(tuple);
			else
				edge.tuples = Arrays.asList(tuple);
			return false;
		}
		else
			return graph.addEdge(prefix+sourceState, prefix+targetState, new ActorVerificationEdge(null, Arrays.asList(tuple)));
	};
	
	public boolean addInTransition(String sourceState, String targetState, Set<Integer> events) {
		return addTransition(sourceState, targetState, events, null);
	};
	
	public boolean addInTransition(String sourceState, String targetState, int event) {
		return addTransition(sourceState, targetState, event);
	};
	
	public boolean addOutTransition(String sourceState, String targetState, List<ActorVerficationMessageTuple> tuples) {
		return addTransition(sourceState, targetState, null, tuples);
	}
	
	public boolean addOutTransition(String sourceState, String targetState, ActorVerficationMessageTuple tuple) {
		return addTransition(sourceState, targetState, tuple);
	}
	
	public boolean addOutTransition(String sourceState, String targetState, int event, String... aliases) {
		return addTransition(sourceState, targetState, new ActorVerficationMessageTuple(new HashSet<Integer>(Arrays.asList(event)), Arrays.asList(aliases)));
	}
}
