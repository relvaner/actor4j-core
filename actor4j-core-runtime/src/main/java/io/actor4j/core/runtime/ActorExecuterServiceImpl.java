/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.runtime;

import static io.actor4j.core.logging.ActorLogger.*;
import static io.actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorWithDistributedGroup;
import io.actor4j.core.runtime.failsafe.ErrorHandler;
import io.actor4j.core.runtime.failsafe.FailsafeManager;
import io.actor4j.core.runtime.persistence.ActorPersistenceService;
import io.actor4j.core.runtime.persistence.ActorPersistenceServiceImpl;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupList;
import io.actor4j.core.utils.ActorTimer;

public abstract class ActorExecuterServiceImpl implements ActorExecuterService {
	protected final InternalActorRuntimeSystem system;
	
	protected final FailsafeManager failsafeManager;

	protected /*quasi final*/ Runnable onTermination;
	
	protected final AtomicBoolean started;
	
	protected /*quasi final*/ ActorTimerExecuterService globalTimerExecuterService;
	protected /*quasi final*/ ActorTimerExecuterService timerExecuterService;
	protected /*quasi final*/ ExecutorService resourceExecuterService;
	
	protected /*quasi final*/ ActorPersistenceService persistenceService;
	
	protected /*quasi final*/ ScheduledExecutorService podReplicationControllerExecuterService;
	protected /*quasi final*/ PodReplicationControllerRunnable podReplicationControllerRunnable;
	
	protected /*quasi final*/ ScheduledExecutorService watchdogExecuterService;
	protected /*quasi final*/ WatchdogRunnable watchdogRunnable;

	public ActorExecuterServiceImpl(final InternalActorRuntimeSystem system) {
		super();
		
		this.system = system;
		
		started = new AtomicBoolean();

		failsafeManager = new FailsafeManager(new ErrorHandler() {
			@Override
			public void handle(Throwable t, String message, UUID uuid) {
				if (message!=null) {
					if (message.equals("initialization")) {
						systemLogger().log(ERROR,
							String.format("[SAFETY] Exception in initialization of an actor"));
					}
					else if (message.equals("actor") || message.equals("resource")) {
						Actor actor = system.getCells().get(uuid).getActor();
						systemLogger().log(ERROR,
								String.format("[SAFETY] Exception in actor: %s", actorLabel(actor)));
					}
					else if (message.equals("pseudo")) {
						Actor actor = system.getPseudoCells().get(uuid).getActor();
						systemLogger().log(ERROR,
								String.format("[SAFETY] Exception in actor: %s", actorLabel(actor)));
					}
					else if (message.equals("replication")) {
						systemLogger().log(ERROR,
								String.format("[SAFETY][FATAL] Exception in PodReplicationControllerThread"));
					}
					else if (message.equals("watchdog")) {
						systemLogger().log(ERROR,
								String.format("[FAILSAFE] Exception in WatchdogThread"));
					}
					else if (message.equals("executer_resource")) {
						Actor actor = system.getCells().get(uuid).getActor();
						systemLogger().log(ERROR,
								String.format("[SAFETY][EXECUTER][REJECTION] Exception in resource actor: %s", actorLabel(actor)));
					}
					else if (message.equals("executer_client")) {
						systemLogger().log(ERROR,
								String.format("[SAFETY][EXECUTER][REJECTION] Exception in sending a message as a client"));
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
	
	@Override
	public FailsafeManager getFailsafeManager() {
		return failsafeManager;
	}
	
	@Override
	public ActorPersistenceService getPersistenceService() {
		return persistenceService;
	}

	@Override
	public void run(Runnable onStartup) {
		start(onStartup, null);
	}
	
	@Override
	public void start(Runnable onStartup, Runnable onTermination) {
		if (system.getCells().size()==0)
			return;
		
		int poolSize = Runtime.getRuntime().availableProcessors();
		
		globalTimerExecuterService = new ActorTimerExecuterService(system, 1, "actor4j-global-timer-thread");
		timerExecuterService = new ActorTimerExecuterService(system, poolSize);
		
		resourceExecuterService = new ThreadPoolExecutor(poolSize, system.getConfig().maxResourceThreads(), 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("actor4j-resource-thread"));
		
		if (system.getConfig().persistenceMode()) {
			persistenceService = new ActorPersistenceServiceImpl(system, system.getConfig().parallelism(), system.getConfig().parallelismFactor(), system.getConfig().persistenceDriver());
			persistenceService.start();
		}
		
		this.onTermination = onTermination;
		
		/* necessary before actorThreadPool instantiation */
		ActorGroup watchdogActorGroup = new ActorGroupList();
		final AtomicInteger watchdogIndex = new AtomicInteger(0);
		List<UUID> watchdogActors = system.addSystemActor(() -> new ActorWithDistributedGroup("watchdog-"+watchdogIndex.getAndIncrement(), watchdogActorGroup) {
			@Override
			public void receive(ActorMessage<?> message) {
				// empty
			}
		}, system.getConfig().parallelism()*system.getConfig().parallelismFactor());
		watchdogRunnable = system.getWatchdogRunnableFactory().apply(system, watchdogActors);
		
		createActorProcessPool();
		
		podReplicationControllerExecuterService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("actor4j-replication-controller-thread"));
		podReplicationControllerRunnable = system.getPodReplicationControllerRunnableFactory().apply(system);
		if (podReplicationControllerRunnable!=null)
			podReplicationControllerExecuterService.scheduleAtFixedRate(podReplicationControllerRunnable, system.getConfig().horizontalPodAutoscalerSyncTime(), system.getConfig().horizontalPodAutoscalerSyncTime(), TimeUnit.MILLISECONDS);
		
		watchdogExecuterService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("actor4j-watchdog-thread"));
		
		/*
		 * necessary before executing onStartup; 
		 * creating of childrens in Actor::preStart: childrens needs to register at the dispatcher
		 * (see also ActorSystemImpl::internal_addCell)
		 */
		started.set(true);
		
		if (onStartup!=null)
			onStartup.run();
		
		if (watchdogRunnable!=null)
			watchdogExecuterService.scheduleAtFixedRate(watchdogRunnable, system.getConfig().watchdogSyncTime(), system.getConfig().watchdogSyncTime(), TimeUnit.MILLISECONDS);
	}
	
	public abstract void createActorProcessPool();
	public abstract void shutdownActorProcessPool(Runnable onTermination, boolean await);
	
	@Override
	public boolean isStarted() {
		return started.get();
	}

	@Override
	public ActorTimer timer() {
		return timerExecuterService;
	}
	
	@Override
	public ActorTimer globalTimer() {
		return globalTimerExecuterService;
	}
	
	@Override
	public void resource(final ActorMessage<?> message) {
		final ResourceActorCell cell = (ResourceActorCell)system.getCells().get(message.dest());
		if (cell!=null && cell.beforeRun(message)) {
			if (!resourceExecuterService.isShutdown())
				try {
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
				catch (RejectedExecutionException e) {
					failsafeManager.notifyErrorHandler(e, "executer_resource", cell.getId());
				}
		}
	}
	
	@Override
	public void shutdown(boolean await) {
		watchdogExecuterService.shutdown();
		podReplicationControllerExecuterService.shutdown();
		
		globalTimerExecuterService.shutdown();
		timerExecuterService.shutdown();
		
		resourceExecuterService.shutdown();
		
		shutdownActorProcessPool(onTermination, await);
		
		if (system.getConfig().persistenceMode())
			persistenceService.shutdown();
		
		reset();
	}
	
	@Override
	public long getCount() {
		return 0;
	}
	
	@Override
	public List<Long> getCounts() {
		return new ArrayList<>();
	}

	public boolean isResponsiveThread(int index) {
		return watchdogRunnable!=null ? watchdogRunnable.isResponsiveThread(index) : true;
	}
	
	@Override
	public Set<Long> nonResponsiveThreads() {
		return watchdogRunnable!=null ? watchdogRunnable.nonResponsiveThreads() : new HashSet<>();
	}
	
	@Override
	public int nonResponsiveThreadsCount() {	
		return watchdogRunnable!=null ? watchdogRunnable.nonResponsiveThreadsCount() : 0;
	}
}
