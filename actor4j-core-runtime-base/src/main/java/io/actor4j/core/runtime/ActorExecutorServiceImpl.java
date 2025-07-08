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
import io.actor4j.core.exceptions.ActorInitializationException;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.runtime.fault.tolerance.ErrorHandler;
import io.actor4j.core.runtime.fault.tolerance.FaultToleranceManager;
import io.actor4j.core.runtime.persistence.ActorPersistenceService;
import io.actor4j.core.runtime.persistence.ActorPersistenceServiceImpl;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupList;
import io.actor4j.core.utils.ActorTimer;

public abstract class ActorExecutorServiceImpl<U extends ActorExecutionUnit> implements InternalActorExecutorService<U> {
	protected final InternalActorRuntimeSystem system;
	
	protected final FaultToleranceManager faultToleranceManager;

	protected /*quasi final*/ Runnable onTermination;
	
	protected final AtomicBoolean started;
	
	protected /*quasi final*/ ActorExecutionUnitPool<U> executionUnitPool;
	
	protected /*quasi final*/ ActorTimerExecutorService globalTimerExecutorService;
	protected /*quasi final*/ ActorTimerExecutorService timerExecutorService;
	protected /*quasi final*/ ExecutorService resourceExecutorService;
	
	protected /*quasi final*/ ActorPersistenceService persistenceService;
	
	protected /*quasi final*/ ScheduledExecutorService podReplicationControllerExecutorService;
	protected /*quasi final*/ PodReplicationControllerRunnable podReplicationControllerRunnable;
	
	protected /*quasi final*/ ScheduledExecutorService watchdogExecutorService;
	protected /*quasi final*/ WatchdogRunnable watchdogRunnable;

	public ActorExecutorServiceImpl(final InternalActorRuntimeSystem system) {
		super();
		
		this.system = system;
		
		started = new AtomicBoolean();

		faultToleranceManager = new FaultToleranceManager(new ErrorHandler() {
			@Override
			public void handle(Throwable t, ActorSystemError systemError, String message, Object faultToleranceId) {
				if (t instanceof ActorInitializationException) {
					systemLogger().log(ERROR,
						String.format("[FT] Exception in initialization of an actor"));
				}
				else if (systemError!=null) {
					if (systemError==ActorSystemError.ACTOR || systemError==ActorSystemError.RESOURCE_ACTOR) {
						InternalActorCell cell = (InternalActorCell)faultToleranceId;
						if (cell!=null) {
							Actor actor = cell.getActor();
							systemLogger().log(ERROR,
								String.format("[FT] Exception in actor: %s", actorLabel(actor)));
						}
						else
							systemLogger().log(ERROR,
								String.format("[FT] Exception in actor: %s", faultToleranceId.toString()));
					}
					else if (systemError==ActorSystemError.EMBEDDED_ACTOR) {
						InternalActorCell cell = (InternalActorCell)faultToleranceId;
						if (cell!=null) {
							Actor actor = cell.getActor();
							systemLogger().log(ERROR,
								String.format("[FT] Exception in embedded actor: %s (host: %s)", message, actorLabel(actor)));
						}
						else
							systemLogger().log(ERROR,
								String.format("[FT] Exception in embedded actor: %s (host: %s)", message, faultToleranceId.toString()));
						
					}
					else if (systemError==ActorSystemError.EMBEDDED_MODULE) {
						InternalActorCell cell = (InternalActorCell)faultToleranceId;
						if (cell!=null) {
							Actor actor = cell.getActor();
							systemLogger().log(ERROR,
								String.format("[FT] Exception in embedded module: %s (host: %s)", message, actorLabel(actor)));
						}
						else
							systemLogger().log(ERROR,
								String.format("[FT] Exception in embedded module: %s (host: %s)", message, faultToleranceId.toString()));
					}
					else if (systemError==ActorSystemError.PSEUDO_ACTOR) {
						InternalActorCell cell = system.getPseudoCells().get(faultToleranceId);
						if (cell!=null) {
							Actor actor = cell.getActor();
							systemLogger().log(ERROR,
								String.format("[FT] Exception in actor: %s", actorLabel(actor)));
						}
						else
							systemLogger().log(ERROR,
								String.format("[FT] Exception in actor: %s", faultToleranceId.toString()));
					}
					else if (systemError==ActorSystemError.REPLICATION) {
						systemLogger().log(ERROR,
								String.format("[FT][FATAL] Exception in PodReplicationControllerThread"));
					}
					else if (systemError==ActorSystemError.WATCHDOG) {
						systemLogger().log(ERROR,
								String.format("[FT] Exception in WatchdogThread"));
					}
					else if (systemError==ActorSystemError.EXECUTER_RESOURCE) {
						InternalActorCell cell = (InternalActorCell)faultToleranceId;
						if (cell!=null) {
							Actor actor = cell.getActor();
							systemLogger().log(ERROR,
								String.format("[FT][EXECUTOR][REJECTION] Exception in resource actor: %s", actorLabel(actor)));
						}
						else
							systemLogger().log(ERROR,
								String.format("[FT][EXECUTOR][REJECTION] Exception in resource actor: %s", faultToleranceId.toString()));
					}
					else if (systemError==ActorSystemError.EXECUTER_CLIENT) {
						systemLogger().log(ERROR,
								String.format("[FT][EXECUTOR][REJECTION] Exception in sending a message as a client"));
					}
				}
				else {
					systemLogger().log(ERROR,
						String.format("[FT][FATAL] Exception in Thread/Runnable"));
				}
				
				t.printStackTrace();
			}
		});
	}
	
	protected void reset() {
		started.set(false);
	}
	
	@Override
	public FaultToleranceManager getFaultToleranceManager() {
		return faultToleranceManager;
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
//		if (system.isEmpty())
//			return;
		
		int poolSize = Runtime.getRuntime().availableProcessors();
		
		globalTimerExecutorService = new ActorTimerExecutorService(system, 1, "actor4j-global-timer-thread");
		timerExecutorService = new ActorTimerExecutorService(system, poolSize);
		
		createActorResourcePool(poolSize);
		
		if (system.getConfig().persistenceMode()) {
			persistenceService = new ActorPersistenceServiceImpl(system, system.getConfig().parallelism(), system.getConfig().parallelismFactor(), system.getConfig().persistenceDriver());
			persistenceService.start();
		}
		
		this.onTermination = onTermination;
		
		if (system.getConfig().watchdogEnabled()) {
			/* necessary before actorThreadPool instantiation */
			ActorGroup watchdogActorGroup = new ActorGroupList();
			final AtomicInteger watchdogIndex = new AtomicInteger(0);
			List<ActorId> watchdogActors = system.addSystemActor(() -> new ActorWithDistributedGroup("watchdog-"+watchdogIndex.getAndIncrement(), watchdogActorGroup) {
				@Override
				public void receive(ActorMessage<?> message) {
					// empty
				}
			}, system.getConfig().parallelism()*system.getConfig().parallelismFactor());
			watchdogRunnable = system.getWatchdogRunnableFactory().apply(system, watchdogActors);
		}
		
		executionUnitPool = createExecutionUnitPool();
		
		if (system.getConfig().horizontalPodAutoscalerEnabled()) {
			podReplicationControllerExecutorService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("actor4j-replication-controller-thread"));
			podReplicationControllerRunnable = system.getPodReplicationControllerRunnableFactory().apply(system);
			if (podReplicationControllerRunnable!=null)
				podReplicationControllerExecutorService.scheduleAtFixedRate(podReplicationControllerRunnable, system.getConfig().horizontalPodAutoscalerSyncTime(), system.getConfig().horizontalPodAutoscalerSyncTime(), TimeUnit.MILLISECONDS);
		}
		
		if (system.getConfig().watchdogEnabled())
			watchdogExecutorService = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("actor4j-watchdog-thread"));
		
		/*
		 * necessary before executing onStartup; 
		 * creating of childrens in Actor::preStart: childrens needs to register at the dispatcher
		 * (see also ActorSystemImpl::internal_addCell)
		 */
		started.set(true);
		
		if (onStartup!=null)
			onStartup.run();
		
		if (system.getConfig().watchdogEnabled() && watchdogRunnable!=null)
			watchdogExecutorService.scheduleAtFixedRate(watchdogRunnable, system.getConfig().watchdogSyncTime(), system.getConfig().watchdogSyncTime(), TimeUnit.MILLISECONDS);
	}
	
	public void createActorResourcePool(int poolSize) {
		resourceExecutorService = new ThreadPoolExecutor(poolSize, system.getConfig().maxResourceThreads(), 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("actor4j-resource-thread"));
	}
	
	public void shutdownActorResourcePool(boolean await) {
		resourceExecutorService.shutdownNow();
		if (await)
			try {
				resourceExecutorService.awaitTermination(system.getConfig().awaitTerminationTimeout(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	public abstract ActorExecutionUnitPool<U> createExecutionUnitPool();
	
	public void shutdownExecutionUnitPool(Runnable onTermination, boolean await) {
		executionUnitPool.shutdown(onTermination, await);
	}
	
	@Override
	public ActorExecutionUnitPool<U> getExecutionUnitPool() {
		return executionUnitPool;
	}
	
	@Override
	public boolean isStarted() {
		return started.get();
	}

	@Override
	public ActorTimer timer() {
		return timerExecutorService;
	}
	
	@Override
	public ActorTimer globalTimer() {
		return globalTimerExecutorService;
	}
	
	@Override
	public void resource(final ActorMessage<?> message) {
		final ResourceActorCell cell = (ResourceActorCell)message.dest();
		if (cell!=null && cell.beforeRun(message)) {
			if (!resourceExecutorService.isShutdown())
				try {
					resourceExecutorService.submit(new Runnable() {
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
					faultToleranceManager.notifyErrorHandler(e, ActorSystemError.EXECUTER_RESOURCE, cell.getId());
				}
		}
	}
	
	@Override
	public void shutdown(boolean await) {
		if (system.getConfig().watchdogEnabled()) {
			watchdogExecutorService.shutdownNow();
			if (await)
				try {
					watchdogExecutorService.awaitTermination(system.getConfig().awaitTerminationTimeout(), TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (system.getConfig().horizontalPodAutoscalerEnabled()) {
			podReplicationControllerExecutorService.shutdownNow();
			if (await)
				try {
					podReplicationControllerExecutorService.awaitTermination(system.getConfig().awaitTerminationTimeout(), TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		globalTimerExecutorService.shutdown();
		timerExecutorService.shutdown();
		
		shutdownActorResourcePool(await);
		
		shutdownExecutionUnitPool(onTermination, await);
		
		if (system.getConfig().persistenceMode())
			persistenceService.shutdown();
		
		reset();
	}
	
	@Override
	public long getCount() {
		return executionUnitPool!=null ? executionUnitPool.getCount() : 0;
	}
	
	@Override
	public List<Long> getCounts() {
		return executionUnitPool!=null ? executionUnitPool.getCounts() : new ArrayList<>();
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
