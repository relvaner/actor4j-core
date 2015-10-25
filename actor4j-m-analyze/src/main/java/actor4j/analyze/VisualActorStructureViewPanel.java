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

import actor4j.core.ActorCell;
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
			
	public void analyzeStructure(Map<UUID, ActorCell> actorCells, boolean showDefaultRoot) {
		Iterator<Entry<UUID, Boolean>> iteratorActiveCells = activeCells.entrySet().iterator();
		while (iteratorActiveCells.hasNext())
			iteratorActiveCells.next().setValue(false);
		changed = false;
		
        graph.getModel().beginUpdate();
        try {
        	if (showDefaultRoot && defaultRoot==null)
        		defaultRoot = addVertex("actor4j", ";fillColor=white");
        	
        	analyzeRootActor(actorCells, actorCells.get(system.USER_ID),    ";fillColor=yellow", showDefaultRoot);
        	analyzeRootActor(actorCells, actorCells.get(system.SYSTEM_ID),  ";fillColor=yellow", showDefaultRoot);
        	analyzeRootActor(actorCells, actorCells.get(system.UNKNOWN_ID), ";fillColor=yellow", showDefaultRoot);
        	
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
	
	public void analyzeRootActor(Map<UUID, ActorCell> actorCells, ActorCell root, String color, boolean showDefaultRoot) {
		if (root!=null) {
			if (activeCells.put(root.getId(), true)==null) {
				Object rootVertex;
				if (root.getActor().getName()!=null)
					rootVertex = addVertex(root.getActor().getName(), color);
				else
					rootVertex = addVertex(root.getId().toString(), color);
			
				if (showDefaultRoot)
					addEdge(null, defaultRoot, rootVertex);
				
				cells.put(root.getId(), rootVertex);
				changed = true;
			}
			
			analyzeActor(actorCells, root, cells.get(root.getId()));
		}
	}
	
	public void analyzeActor(Map<UUID, ActorCell> actorCells, ActorCell parent, Object parentVertex) {
		Iterator<UUID> iterator = parent.getChildren().iterator();
		while (iterator.hasNext()) {
			ActorCell child = actorCells.get(iterator.next());
			if (activeCells.put(child.getId(), true)==null) {
				Object childVertex;
				if (child.getActor().getName()!=null)
					childVertex = addVertex(child.getActor().getName(), ";fillColor=#00FF00");
				else
					childVertex = addVertex(child.getId().toString(), ";fillColor=#00FF00");
			
				addEdge(null, parentVertex, childVertex);
				
				cells.put(child.getId(), childVertex);
				changed = true;
			}
			
			analyzeActor(actorCells, child, cells.get(child.getId()));
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
