/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.verification;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;

public class ActorVerificationUtils {
	// find cycles
	public static <V, E> List<List<V>> findCycles(DirectedGraph<V, E> graph) {
		return new JohnsonSimpleCycles<>(graph).findSimpleCycles();
	}
	
	// find unreachable vertexes from the start vertex
	public static <V, E> Set<V> findUnreachables(DirectedGraph<V, E> graph, V startVertex) {  
		Set<V> result = new HashSet<>();
		 
		for (V endVertex : graph.vertexSet())
			if (endVertex!=startVertex && DijkstraShortestPath.findPathBetween(graph, startVertex, endVertex)==null)
				result.add(endVertex);
		 
		return result;
	} 
	
	// find vertexes that do not have outgoing connections
	public static <V, E> Set<V> findDead(DirectedGraph<V, E> graph, Set<V> unreachables) {
		Set<V> result = new HashSet<>(graph.vertexSet());
		result.removeAll(unreachables);
		
		Iterator<V> iterator = result.iterator();
		while (iterator.hasNext()) 
			if (graph.outDegreeOf(iterator.next())>0)
				iterator.remove();
		
		return result;
	}
}
