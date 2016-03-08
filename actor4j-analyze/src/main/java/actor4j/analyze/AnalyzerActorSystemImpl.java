/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.analyze;

import java.util.concurrent.atomic.AtomicBoolean;

import actor4j.core.ActorSystem;
import actor4j.core.ActorSystemImpl;
import actor4j.core.DefaultActorSystemImpl;

public class AnalyzerActorSystemImpl extends DefaultActorSystemImpl {
	protected AtomicBoolean analyzeMode;
	protected ActorAnalyzerThread analyzerThread;

	public AnalyzerActorSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}
	
	public AnalyzerActorSystemImpl (String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		analyzeMode = new AtomicBoolean(false);
		
		messageDispatcher = new AnalyzerActorMessageDispatcher(this);
	}
	
	public ActorSystemImpl analyze(ActorAnalyzerThread analyzerThread) {
		if (!executerService.isStarted()) {
			this.analyzerThread = analyzerThread;
			if (analyzerThread!=null) {
				analyzerThread.setSystem(this);
				analyzeMode.set(true);
			}
		}
		
		return this;
	}
	
	public ActorAnalyzerThread getAnalyzerThread() {
		return analyzerThread;
	}

	public AtomicBoolean getAnalyzeMode() {
		return analyzeMode;
	}
	
	public void start(Runnable onTermination) {
		if (!executerService.isStarted())
			if (analyzeMode.get())
				analyzerThread.start();
		super.start(onTermination);
	}
	
	@Override
	public void shutdownWithActors(final boolean await) {
		if (executerService.isStarted())
			if (analyzeMode.get()) {
				analyzeMode.set(false);
				analyzerThread.interrupt();
			}
		super.shutdownWithActors(await);
	}
	
	@Override
	public void shutdown(boolean await) {
		if (executerService.isStarted())
			if (analyzeMode.get()) {
				analyzeMode.set(false);
				analyzerThread.interrupt();
			}
		super.shutdown(await);
	}
}
