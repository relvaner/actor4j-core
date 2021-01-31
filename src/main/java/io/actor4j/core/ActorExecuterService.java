/*
 * Copyright (c) 2015-2020, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.failsafe.ErrorHandler;
import io.actor4j.core.failsafe.FailsafeManager;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.persistence.ActorPersistenceService;

public class ActorExecuterService {
	protected final ActorSystemImpl system;
	
	protected final FailsafeManager failsafeManager;
	
	protected ActorThreadPool actorThreadPool;
	protected Runnable onTermination;
	
	protected final AtomicBoolean started;
	
	protected ActorTimerExecuterService globalTimerExecuterService;
	protected ActorTimerExecuterService timerExecuterService;
	protected ExecutorService clientExecuterService;
	protected ExecutorService resourceExecuterService;
	
	protected ActorPersistenceService persistenceService;
	
	protected ScheduledExecutorService podReplicationControllerExecuterService;
	protected PodReplicationControllerRunnable podReplicationControllerRunnable;
	
	protected int maxResourceThreads;
	
	public ActorExecuterService(final ActorSystemImpl system) {
		super();
		
		this.system = system;
		
		started = new AtomicBoolean();
		
		maxResourceThreads = 200;
		
		failsafeManager = new FailsafeManager();
		failsafeManager.setErrorHandler(new ErrorHandler() {
			@Override
			public void handle(Throwable t, String message, UUID uuid) {
				if (message!=null) {
					if (message.equals("initialization")) {
						systemLogger().log(ERROR,
							String.format("[SAFETY] Exception in initialization of an actor"));
					}
					else if (message.equals("actor") || message.equals("resource")) {
						Actor actor = system.cells.get(uuid).actor;
						systemLogger().log(ERROR,
								String.format("[SAFETY] Exception in actor: %s", actorLabel(actor)));
					}
					else if (message.equals("pseudo")) {
						Actor actor = system.pseudoCells.get(uuid).actor;
						systemLogger().log(ERROR,
								String.format("[SAFETY] Exception in actor: %s", actorLabel(actor)));
					}
					else if (message.equals("replication")) {
						systemLogger().log(ERROR,
								String.format("[SAFETY][FATAL] Exception in PodReplicationControllerThread"));
					}
				}
				else {
					systemLogger().log(ERROR,
						String.format("[SAFETY][FATAL] Exception in ActorThread"));
				}
				
				t.printStackTrace();
			}
		});
	}
	
	protected void reset() {
		started.set(false);
	}
	
	public FailsafeManager getFailsafeManager() {
		return failsafeManager;
	}
	
	public ActorThreadPool getActorThreadPool() {
		return actorThreadPool;
	}

	public void run(Runnable onStartup) {
		start(onStartup, null);
	}
	
	public void start(Runnable onStartup, Runnable onTermination) {
		if (system.cells.size()==0)
			return;
		
		int poolSize = Runtime.getRuntime().availableProcessors();
		
		globalTimerExecuterService = new ActorTimerExecuterService(system, 1, "actor4j-global-timer-thread");
		timerExecuterService = new ActorTimerExecuterService(system, poolSize);
		
		resourceExecuterService = new ThreadPoolExecutor(poolSize, maxResourceThreads, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("actor4j-resource-thread"));
		if (system.clientMode)
			clientExecuterService = Executors.newSingleThreadExecutor();
		
		if (system.persistenceMode) {
			persistenceService = new ActorPersistenceService(system.wrapper, system.parallelismMin, system.parallelismFactor, system.persistenceConnector);
			persistenceService.start();
		}
		
		this.onTermination = onTermination;
		
		actorThreadPool = new ActorThreadPool(system);
		
		podReplicationControllerExecuterService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("actor4j-replication-controller-thread"));
		podReplicationControllerRunnable = system.podReplicationControllerRunnableFactory.apply(system);
		
		if (podReplicationControllerRunnable!=null)
			podReplicationControllerExecuterService.scheduleAtFixedRate(podReplicationControllerRunnable, system.horizontalPodAutoscalerSyncTime, system.horizontalPodAutoscalerSyncTime, TimeUnit.MILLISECONDS);
		
		/*
		 * necessary before executing onStartup; 
		 * creating of childrens in Actor::preStart: childrens needs to register at the dispatcher
		 * (see also ActorSystemImpl::internal_addCell)
		 */
		started.set(true);
		
		if (onStartup!=null)
			onStartup.run();
	}
	
	public boolean isStarted() {
		return started.get();
	}

	public ActorTimer timer() {
		return timerExecuterService;
	}
	
	public ActorTimer globalTimer() {
		return globalTimerExecuterService;
	}

	public void clientViaAlias(final ActorMessage<?> message, final String alias) {
		if (system.clientRunnable!=null)
			clientExecuterService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						system.clientRunnable.runViaAlias(message, alias);
					}
					catch(Throwable t) {
						t.printStackTrace();
					}	
				}
			});
	}
	
	public void clientViaPath(final ActorMessage<?> message, final ActorServiceNode node, final String path) {
		if (system.clientRunnable!=null)
			clientExecuterService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						system.clientRunnable.runViaPath(message, node, path);
					}
					catch(Throwable t) {
						t.printStackTrace();
					}	
				}
			});
	}
	
	public void resource(final ActorMessage<?> message) {
		final ResourceActorCell cell = (ResourceActorCell)system.cells.get(message.dest);
		if (cell!=null && cell.beforeRun(message)) {
			resourceExecuterService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						cell.run(message);
					}
					catch(Throwable t) {
						t.printStackTrace();
					}	
				}
			});
		}
	}
	
	public void shutdown(boolean await) {
		podReplicationControllerExecuterService.shutdown();
		
		globalTimerExecuterService.shutdown();
		timerExecuterService.shutdown();
		
		resourceExecuterService.shutdown();
		if (system.clientMode)
			clientExecuterService.shutdown();
		
		actorThreadPool.shutdown(onTermination, await);
		
		if (system.persistenceMode)
			persistenceService.shutdown();
		
		reset();
	}
	
	public long getCount() {
		return actorThreadPool!=null ? actorThreadPool.getCount() : 0;
	}
	public List<Long> getCounts() {
		return actorThreadPool!=null ? actorThreadPool.getCounts() : new ArrayList<>();
	}
}
