/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.analyzer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import actor4j.analyzer.visual.VisualActorAnalyzer;
import actor4j.core.ActorCell;
import actor4j.core.ActorSystemImpl;
import actor4j.core.messages.ActorMessage;

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
	protected void setSystem(ActorSystemImpl system) {
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
	protected void update(final Map<UUID, ActorCell> cells) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				visualAnalyzer.analyzeStructure(cells, showDefaultRoot);
				visualAnalyzer.analyzeBehaviour(cells, deliveryRoutes);
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
