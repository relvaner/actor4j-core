package actor4j.analyze;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxCell;

import java.util.Map.Entry;

import actor4j.core.Actor;
import actor4j.core.ActorSystem;

public class VisualActorBehaviourViewPanel extends VisualActorViewPanel  {
	protected static final long serialVersionUID = 9212208191147321764L;
	
	protected Map<UUID, Boolean> activeCells;
	protected Map<UUID, Object>  cells;
	
	protected boolean changed;

	public VisualActorBehaviourViewPanel(ActorSystem system) {
		super(system);
		
		activeCells = new HashMap<>();
		cells = new HashMap<>();
		
		add("Behaviour", paDesign);
	}
	
	public void analyzeBehaviour(Map<UUID, Actor> actors, Map<UUID, Map<UUID, Long>> deliveryRoutes) {
		Iterator<Entry<UUID, Boolean>> iteratorActiveCells = activeCells.entrySet().iterator();
		while (iteratorActiveCells.hasNext())
			iteratorActiveCells.next().setValue(false);
		changed = false;
		
		String color = null;
		graph.getModel().beginUpdate();
	    try {
	    	Iterator<Actor> iterator = actors.values().iterator();
	        while (iterator.hasNext()) {
	        	Actor actor = iterator.next(); 
	        	if (activeCells.put(actor.getId(), true)==null) {
	        		if (actor.getId()==system.USER_ID || actor.getId()==system.SYSTEM_ID || actor.getId()==system.UNKNOWN_ID)
	        			color = ";fillColor=yellow";
	        		else
	        			color = ";fillColor=#00FF00";
	        		
    				Object vertex;
    				if (actor.getName()!=null)
    					vertex = addVertex(actor.getName(), color);
    				else
    					vertex = addVertex(actor.getId().toString(), color);
    			
    				cells.put(actor.getId(), vertex);
    				changed = true;
    			}
	        }
	    	 
	        iteratorActiveCells = activeCells.entrySet().iterator();
     		while (iteratorActiveCells.hasNext()) {
     			Entry<UUID, Boolean> entry = iteratorActiveCells.next();
     			if (!entry.getValue()) {
     				graph.removeCells(graph.getChildVertices(cells.get(entry.getKey())));
     				cells.remove(entry.getKey());
     				iteratorActiveCells.remove();
     				changed = true;
     			}		
     		}
     			
     		Iterator<Entry<UUID, Object>> iteratorCells = cells.entrySet().iterator();
     		while (iteratorCells.hasNext()) {
     			Entry<UUID, Object> entry = iteratorCells.next();
     			
     			analyzeDeliveryRoutes(deliveryRoutes, entry);
     		}
     		
	    } finally {
			graph.getModel().endUpdate();
		}
	    graphComponent.refresh();
	}
	
	public void analyzeDeliveryRoutes(Map<UUID, Map<UUID, Long>> deliveryRoutes, Entry<UUID, Object> entrySource) {
		Map<UUID, Long> routes = deliveryRoutes.get(entrySource.getKey());
		
		if (routes!=null) {
			Object source = entrySource.getValue();
			Iterator<Entry<UUID, Long>> iterator = routes.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<UUID, Long> entry = iterator.next();
				Object dest = null;
				if ((dest=cells.get(entry.getKey()))!=null) {
					Object[] edges = null;
					if ((edges=graph.getEdgesBetween(source, dest))!=null && edges.length>0) {
						boolean found=false;
						for (Object edge : edges)
							if (((mxCell)edge).getSource()==source && ((mxCell)edge).getTarget()==dest) {
								((mxCell)edge).setValue(entry.getValue());
								found=true;
								break;
							}
						if (!found) {
							addEdge(entry.getValue().toString(), source, dest);
							changed = true;
						}
					}
					else {
						addEdge(entry.getValue().toString(), source, dest);
						changed = true;
					}
				}
			}
		}
		
	}

	@Override
	public void updateStructure() {
		resetViewport();
		
		if (changed) {
			mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
			layout.setForceConstant(60); 			// the higher, the more separated
			layout.setDisableEdgeStyle( false); 	// true transforms the edges and makes them direct lines
			layout.execute(graph.getDefaultParent());

			//new mxCircleLayout(graph).execute(graph.getDefaultParent());
			new mxParallelEdgeLayout(graph).execute(graph.getDefaultParent());
		}
		
		fitViewport();
	}
}
