/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;

import actor4j.core.Actor;
import actor4j.core.ActorSystem;

public class VisualActorStructureViewPanel extends VisualActorViewPanel {
	protected static final long serialVersionUID = -1192782222987329027L;
	
	protected Map<UUID, Boolean> activeCells;
	protected Map<UUID, Object>  cells;
	
	protected Object defaultRoot;
	protected boolean changed;

	public VisualActorStructureViewPanel(ActorSystem system) {
		super(system);
		
		activeCells = new HashMap<>();
		cells = new HashMap<>();
		
		add("Structure", paDesign);
	}
			
	public void analyzeStructure(Map<UUID, Actor> actors, boolean showDefaultRoot) {
		Iterator<Entry<UUID, Boolean>> iteratorActiveCells = activeCells.entrySet().iterator();
		while (iteratorActiveCells.hasNext())
			iteratorActiveCells.next().setValue(false);
		changed = false;
		
        graph.getModel().beginUpdate();
        try {
        	if (showDefaultRoot && defaultRoot==null)
        		defaultRoot = addVertex("actor4j", ";fillColor=white");
        	
        	analyzeRootActor(actors, actors.get(system.USER_ID),    ";fillColor=yellow", showDefaultRoot);
        	analyzeRootActor(actors, actors.get(system.SYSTEM_ID),  ";fillColor=yellow", showDefaultRoot);
        	analyzeRootActor(actors, actors.get(system.UNKNOWN_ID), ";fillColor=yellow", showDefaultRoot);
        	
        	iteratorActiveCells = activeCells.entrySet().iterator();
        	while (iteratorActiveCells.hasNext()) {
        		Entry<UUID, Boolean> entry = iteratorActiveCells.next();
        		if (!entry.getValue()) {
        			//graph.removeCells(graph.getChildVertices(cells.get(entry.getKey())), true);
        			//graph.removeCells(new Object[] {cells.get(entry.getKey())}, true);
        			graph.getModel().remove(cells.get(entry.getKey()));
        			cells.remove(entry.getKey());
        			iteratorActiveCells.remove();
        			changed = true;
        		}		
        	}
		} finally {
			graph.getModel().endUpdate();
		}
        graphComponent.refresh();
	}
	
	public void analyzeRootActor(Map<UUID, Actor> actors, Actor root, String color, boolean showDefaultRoot) {
		if (root!=null) {
			if (activeCells.put(root.id, true)==null) {
				Object rootVertex;
				if (root.name!=null)
					rootVertex = addVertex(root.name, color);
				else
					rootVertex = addVertex(root.id.toString(), color);
			
				if (showDefaultRoot)
					addEdge(null, defaultRoot, rootVertex);
				
				cells.put(root.id, rootVertex);
				changed = true;
			}
			
			analyzeActor(actors, root, cells.get(root.id));
		}
	}
	
	public void analyzeActor(Map<UUID, Actor> actors, Actor parent, Object parentVertex) {
		Iterator<UUID> iterator = parent.children.iterator();
		while (iterator.hasNext()) {
			Actor child = actors.get(iterator.next());
			if (activeCells.put(child.id, true)==null) {
				Object childVertex;
				if (child.name!=null)
					childVertex = addVertex(child.name, ";fillColor=#00FF00");
				else
					childVertex = addVertex(child.id.toString(), ";fillColor=#00FF00");
			
				addEdge(null, parentVertex, childVertex);
				
				cells.put(child.id, childVertex);
				changed = true;
			}
			
			analyzeActor(actors, child, cells.get(child.id));
		}
	}

	@Override
	public void updateStructure() {
		resetViewport();
		 
		if (changed) {
			mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
			layout.setForceConstant(40); 			// the higher, the more separated
			layout.setDisableEdgeStyle( false); 	// true transforms the edges and makes them direct lines
			layout.execute(graph.getDefaultParent());
		
			//new mxCircleLayout(graph).execute(graph.getDefaultParent());
			new mxParallelEdgeLayout(graph).execute(graph.getDefaultParent());
		}
	    
	    fitViewport();
	}
}
