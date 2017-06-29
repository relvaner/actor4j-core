/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.analyzer.visual;

import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.UUID;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystemImpl;
import tools4j.utils.SwingSubApplication;

public class VisualActorAnalyzer {
	protected ActorSystemImpl system;
	protected SwingSubApplication application;
	
	public VisualActorAnalyzer(ActorSystemImpl system) {
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
	
	public void analyzeStructure(Map<UUID, ActorCell> actorCells, boolean showDefaultRoot) {
		((VisualActorFrame)application.getFrame()).analyzeStructure(actorCells, showDefaultRoot);
	}
	
	public void analyzeBehaviour(Map<UUID, ActorCell> actorCells, Map<UUID, Map<UUID, Long>> deliveryRoutes) {
		((VisualActorFrame)application.getFrame()).analyzeBehaviour(actorCells, deliveryRoutes);
	}
}
