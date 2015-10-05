/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import actor4j.core.Actor;
import actor4j.core.ActorAnalyzerThread;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;

public class DefaultActorAnalyzerThread extends ActorAnalyzerThread {
	protected VisualActorAnalyzer visualAnalyzer;
	
	protected Map<UUID, Map<UUID, Long>> deliveryRoutes;
	
	protected boolean showDefaultRoot;
	
	public DefaultActorAnalyzerThread(long delay, boolean showDefaultRoot) {
		super(delay);
		
		this.showDefaultRoot = showDefaultRoot;
		
		deliveryRoutes = new ConcurrentHashMap<>();
	}
	
	@Override
	protected void setSystem(ActorSystem system) {
		super.setSystem(system);
		
		visualAnalyzer = new VisualActorAnalyzer(system);
	}
	
	@Override
	protected void analyze(ActorMessage<?> message) {
		if (message.source==null)
			message.source = system.UNKNOWN_ID;
		if (message.dest==null)
			message.dest = system.UNKNOWN_ID;
		
		Map<UUID, Long> routes = deliveryRoutes.get(message.source);
		if (routes==null) {
			routes = new ConcurrentHashMap<>();
			deliveryRoutes.put(message.source, routes);
		}
		Long count = routes.get(message.dest);
		if (count==null)
			routes.put(message.dest, 1L);
		else
			routes.put(message.dest, count+1);
	}

	@Override
	protected void update(final Map<UUID, Actor> actors) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				visualAnalyzer.analyzeStructure(actors, showDefaultRoot);
				visualAnalyzer.analyzeBehaviour(actors, deliveryRoutes);
			}
		});
	}
	
	@Override
	public void run() {
		visualAnalyzer.start();
		
		super.run();
		
		visualAnalyzer.stop();
	}
}
