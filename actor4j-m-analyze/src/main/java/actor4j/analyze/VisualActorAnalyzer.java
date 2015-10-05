/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.analyze;

import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorSystem;
import tools4j.utils.SwingSubApplication;

public class VisualActorAnalyzer {
	protected ActorSystem system;
	protected SwingSubApplication application;
	
	public VisualActorAnalyzer(ActorSystem system) {
		super();
		
		this.system = system;
	}
	
	public void start() {
		application = new SwingSubApplication();
		application.setTitle("actor4j-analyzer (Structure & Behaviour)");
		application.runApplication(new VisualActorFrame(system));
	}
	
	public void stop() {
		application.getFrame().dispatchEvent(new WindowEvent(application.getFrame(), WindowEvent.WINDOW_CLOSING));
	}
	
	public void analyzeStructure(Map<UUID, Actor> actors, boolean showDefaultRoot) {
		((VisualActorFrame)application.getFrame()).analyzeStructure(actors, showDefaultRoot);
	}
	
	public void analyzeBehaviour(Map<UUID, Actor> actors, Map<UUID, Map<UUID, Long>> deliveryRoutes) {
		((VisualActorFrame)application.getFrame()).analyzeBehaviour(actors, deliveryRoutes);
	}
}
