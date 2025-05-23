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
package io.actor4j.core.config;

import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.persistence.drivers.PersistenceDriver;
import io.actor4j.core.pods.api.Caching;
import io.actor4j.core.pods.api.Database;
import io.actor4j.core.pods.api.Host;
import io.actor4j.core.runtime.ActorThreadMode;

public class ActorSystemConfig {
	private final String name;
	
	private final boolean debugUnhandled;
	private final boolean debugUndelivered;

	private final int parallelism;
	private final int parallelismFactor;
	
	private final int queueSize;
	private final int bufferQueueSize;

	private final int throughput;
	private final int maxSpins;
	private final int highLoad;
	private final ActorThreadMode threadMode;
	private final long sleepTime;
	
	private final int maxResourceThreads;
	
	private final long awaitTerminationTimeout;
	
	// Supervisor
	private final int maxRetries;
	private final long withinTimeRange;
	
	// Persistence
	private final PersistenceDriver persistenceDriver;
	private final boolean persistenceMode;
	
	// Metrics
	private final AtomicBoolean counterEnabled;
	private final AtomicBoolean metricsEnabled;
	private final AtomicBoolean processingTimeEnabled;
	private final int maxProcessingTimeSamples;
	private final AtomicBoolean trackProcessingTimePerActor;
	private final AtomicBoolean trackRequestRatePerActor;
	
	// Pods
	private final boolean horizontalPodAutoscalerEnabled;
	private final long horizontalPodAutoscalerSyncTime;
	private final long horizontalPodAutoscalerMeasurementTime;
	private final Caching<?> podCaching;
	private final Database<?> podDatabase;
	private final Host<?> podHost;
	
	// Watchdog
	private final boolean watchdogEnabled;
	private final long watchdogSyncTime;
	private final long watchdogTimeout;
	
	// As Service
	private final boolean serverMode;
	
	public String name() {
		return name;
	}
	
	public boolean debugUnhandled() {
		return debugUnhandled;
	}
	
	public boolean debugUndelivered() {
		return debugUndelivered;
	}
	
	public int parallelism() {
		return parallelism;
	}
	
	public int parallelismFactor() {
		return parallelismFactor;
	}
	
	public int queueSize() {
		return queueSize;
	}
	
	public int bufferQueueSize() {
		return bufferQueueSize;
	}
	
	public int throughput() {
		return throughput;
	}
	
	public int maxSpins() {
		return maxSpins;
	}
	
	public int highLoad() {
		return highLoad;
	}
	
	public ActorThreadMode threadMode() {
		return threadMode;
	}
	
	public long sleepTime() {
		return sleepTime;
	}
	
	public int maxResourceThreads() {
		return maxResourceThreads;
	}
	
	public long awaitTerminationTimeout() {
		return awaitTerminationTimeout;
	}
	
	public int maxRetries() {
		return maxRetries;
	}
	
	public long withinTimeRange() {
		return withinTimeRange;
	}
	
	public PersistenceDriver persistenceDriver() {
		return persistenceDriver;
	}
	
	public boolean persistenceMode() {
		return persistenceMode;
	}
	
	public AtomicBoolean counterEnabled() {
		return counterEnabled;
	}
	
	public AtomicBoolean metricsEnabled() {
		return metricsEnabled;
	}
	
	public AtomicBoolean processingTimeEnabled() {
		return processingTimeEnabled;
	}
	
	public int maxProcessingTimeSamples() {
		return maxProcessingTimeSamples;
	}
	
	public AtomicBoolean trackProcessingTimePerActor() {
		return trackProcessingTimePerActor;
	}
	
	public AtomicBoolean trackRequestRatePerActor() {
		return trackRequestRatePerActor;
	}
	
	public boolean horizontalPodAutoscalerEnabled() {
		return horizontalPodAutoscalerEnabled;
	}
	
	public long horizontalPodAutoscalerSyncTime() {
		return horizontalPodAutoscalerSyncTime;
	}
	
	public long horizontalPodAutoscalerMeasurementTime() {
		return horizontalPodAutoscalerMeasurementTime;
	}
	
	public Caching<?> podCaching() {
		return podCaching;
	}
	
	public Database<?> podDatabase() {
		return podDatabase;
	}
	
	public Host<?> podHost() {
		return podHost;
	}
	
	public boolean watchdogEnabled() {
		return watchdogEnabled;
	}
	
	public long watchdogSyncTime() {
		return watchdogSyncTime;
	}
	
	public long watchdogTimeout() {
		return watchdogTimeout;
	}
	
	public boolean serverMode() {
		return serverMode;
	}
	
	public static abstract class Builder<T extends ActorSystemConfig> {
		protected String name;
		
		protected boolean debugUnhandled;
		protected boolean debugUndelivered;

		protected int parallelism;
		protected int parallelismFactor;
		
		protected int queueSize;
		protected int bufferQueueSize;

		protected int throughput;
		protected int maxSpins;
		protected int highLoad;
		protected ActorThreadMode threadMode;
		protected long sleepTime;
		
		protected int maxResourceThreads;
		
		protected long awaitTerminationTimeout;
		
		// Supervisor
		protected int maxRetries;
		protected long withinTimeRange;
		
		// Persistence
		protected PersistenceDriver persistenceDriver;
		protected boolean persistenceMode;

		// Metrics
		protected boolean counterEnabled;
		protected boolean metricsEnabled;
		protected boolean processingTimeEnabled;
		protected int maxProcessingTimeSamples;
		protected boolean trackProcessingTimePerActor;
		protected boolean trackRequestRatePerActor;
		
		// Pods
		protected boolean horizontalPodAutoscalerEnabled;
		protected long horizontalPodAutoscalerSyncTime;
		protected long horizontalPodAutoscalerMeasurementTime;
		protected Caching<?> podCaching;
		protected Database<?> podDatabase;
		protected Host<?> podHost;
		
		// Watchdog
		protected boolean watchdogEnabled;
		protected long watchdogSyncTime;
		protected long watchdogTimeout;
		
		// As Service
		protected boolean serverMode;

		public Builder() {
			super();

			name = "actor4j";
			
			debugUnhandled = true;
			debugUndelivered = true;
			
			parallelism(0);
			parallelismFactor = 1;
			
			queueSize = 50_000;
			bufferQueueSize = 10_000;
			
			throughput = 100;
			maxSpins = 100_000;
			calculateHighLoad();
			threadMode = ActorThreadMode.PARK;
			sleepTime = 25;
			
			maxResourceThreads = 200;
			
			awaitTerminationTimeout = 2_000;
			
			// Supervisor
			maxRetries = 3;
			withinTimeRange = 2_000;
			
			// Persistence
			persistenceMode = false;

			// Metrics
			counterEnabled = false;
			metricsEnabled = false;
			processingTimeEnabled = false;
			maxProcessingTimeSamples = 10_000;
			trackProcessingTimePerActor = false;
			trackRequestRatePerActor = false;

			// Pods
			horizontalPodAutoscalerEnabled = true;
			horizontalPodAutoscalerSyncTime = 15_000;
			horizontalPodAutoscalerMeasurementTime = 2_000;
			
			// Watchdog
			watchdogEnabled = true;
			watchdogSyncTime = 5_000;
			watchdogTimeout = 2_000;
		}
		
		public Builder(T config) {
			super();
			this.name = config.name();
			this.debugUnhandled = config.debugUnhandled();
			this.debugUndelivered = config.debugUndelivered();
			this.queueSize = config.queueSize();
			this.bufferQueueSize = config.bufferQueueSize();
			this.parallelism = config.parallelism();
			this.parallelismFactor = config.parallelismFactor();
			this.throughput = config.throughput();
			this.maxSpins = config.maxSpins();
			this.highLoad = config.highLoad();
			this.threadMode = config.threadMode();
			this.sleepTime = config.sleepTime();
			this.maxResourceThreads = config.maxResourceThreads();
			this.awaitTerminationTimeout = config.awaitTerminationTimeout();
			this.maxRetries = config.maxRetries();
			this.withinTimeRange = config.withinTimeRange();
			this.persistenceDriver = config.persistenceDriver();
			this.persistenceMode = config.persistenceMode();
			this.counterEnabled = config.counterEnabled().get();
			this.metricsEnabled = config.metricsEnabled().get();
			this.processingTimeEnabled = config.processingTimeEnabled().get();
			this.maxProcessingTimeSamples = config.maxProcessingTimeSamples();
			this.trackProcessingTimePerActor = config.trackProcessingTimePerActor().get();
			this.trackRequestRatePerActor = config.trackRequestRatePerActor().get();
			this.horizontalPodAutoscalerEnabled = config.horizontalPodAutoscalerEnabled();
			this.horizontalPodAutoscalerSyncTime = config.horizontalPodAutoscalerSyncTime();
			this.horizontalPodAutoscalerMeasurementTime = config.horizontalPodAutoscalerMeasurementTime();
			this.podCaching = config.podCaching();
			this.podDatabase = config.podDatabase();
			this.podHost = config.podHost();
			this.watchdogEnabled = config.watchdogEnabled();
			this.watchdogSyncTime = config.watchdogSyncTime();
			this.watchdogTimeout = config.watchdogTimeout();
			this.serverMode = config.serverMode();
		}

		public Builder<T> name(String name) {
			if (name!=null)
				this.name = name;
			else
				this.name = "actor4j";

			return this;
		}
		
		public Builder<T> debugUnhandled(boolean debugUnhandled) {
			this.debugUnhandled = debugUnhandled;

			return this;
		}
		
		public Builder<T> debugUndelivered(boolean debugUndelivered) {
			this.debugUndelivered = debugUndelivered;

			return this;
		}
		
		public Builder<T> queueSize(int queueSize) {
			this.queueSize = queueSize;

			return this;
		}

		public Builder<T> bufferQueueSize(int bufferQueueSize) {
			this.bufferQueueSize = bufferQueueSize;

			return this;
		}

		public Builder<T> parallelism(int parallelism) {
			if (parallelism<=0)
				this.parallelism = Runtime.getRuntime().availableProcessors();
			else
				this.parallelism = parallelism;

			return this;
		}

		public Builder<T> parallelismFactor(int parallelismFactor) {
			this.parallelismFactor = parallelismFactor;

			return this;
		}
		
		public Builder<T> throughput(int throughput) {
			this.throughput = throughput;
			calculateHighLoad();

			return this;
		}
		
		public Builder<T> maxSpins(int maxSpins) {
			this.maxSpins = maxSpins;
			calculateHighLoad();

			return this;
		}
		
		protected void calculateHighLoad() {
			highLoad = maxSpins / throughput;
		}

		public Builder<T> parkMode() {
			threadMode = ActorThreadMode.PARK;

			return this;
		}

		public Builder<T> sleepMode() {
			threadMode = ActorThreadMode.SLEEP;

			return this;
		}

		public Builder<T> sleepMode(long sleepTime) {
			this.sleepTime = sleepTime;
			threadMode = ActorThreadMode.SLEEP;

			return this;
		}

		public Builder<T> yieldMode() {
			threadMode = ActorThreadMode.YIELD;

			return this;
		}
		
		public Builder<T> hybridMode() {
			threadMode = ActorThreadMode.HYBRID;

			return this;
		}
		
		public Builder<T> maxResourceThreads(int maxResourceThreads) {
			this.maxResourceThreads = maxResourceThreads;

			return this;
		}
		
		public Builder<T> awaitTerminationTimeout(long awaitTerminationTimeout) {
			this.awaitTerminationTimeout = awaitTerminationTimeout;
			
			return this;
		}
		
		public Builder<T> maxRetries(int maxRetries) {
			this.maxRetries = maxRetries;

			return this;
		}
		
		public Builder<T> withinTimeRange(long withinTimeRange) {
			this.withinTimeRange = withinTimeRange;

			return this;
		}
		
		public Builder<T> persistenceMode(PersistenceDriver persistenceDriver) {
			this.persistenceDriver = persistenceDriver;
			this.persistenceMode = true;

			return this;
		}
		
		public Builder<T> counterEnabled(boolean enabled) {
			counterEnabled = enabled;

			return this;
		}
		
		public Builder<T> metricsEnabled(boolean enabled) {
			metricsEnabled = enabled;

			return this;
		}

		public Builder<T> processingTimeEnabled(boolean enabled) {
			processingTimeEnabled = enabled;

			return this;
		}
		
		public Builder<T> maxProcessingTimeSamples(int maxProcessingTimeSamples) {
			this.maxProcessingTimeSamples = maxProcessingTimeSamples;
			
			return this;
		}
		
		public Builder<T> trackProcessingTimePerActor(boolean enabled) {
			trackProcessingTimePerActor = enabled;

			return this;
		}
		
		public Builder<T> trackRequestRatePerActor(boolean enabled) {
			trackRequestRatePerActor = enabled;

			return this;
		}
		
		public Builder<T> horizontalPodAutoscalerEnabled(boolean enabled) {
			this.horizontalPodAutoscalerEnabled = enabled;
			
			return this;
		}
		
		public Builder<T> horizontalPodAutoscalerSyncTime(long horizontalPodAutoscalerSyncTime) {
			this.horizontalPodAutoscalerSyncTime = horizontalPodAutoscalerSyncTime;
			
			return this;
		}

		public Builder<T> horizontalPodAutoscalerMeasurementTime(long horizontalPodAutoscalerMeasurementTime) {
			this.horizontalPodAutoscalerMeasurementTime = horizontalPodAutoscalerMeasurementTime;
			
			return this;
		}
		
		public Builder<T> podCaching(Caching<?> podCaching) {
			this.podCaching = podCaching;

			return this;
		}

		public Builder<T> podDatabase(Database<?> podDatabase) {
			this.podDatabase = podDatabase;

			return this;
		}
		
		public Builder<T> podHost(Host<?> podHost) {
			this.podHost = podHost;

			return this;
		}
		
		public Builder<T> watchdogEnabled(boolean enabled) {
			this.watchdogEnabled = enabled;
			
			return this;
		}
		
		public Builder<T> watchdogSyncTime(long watchdogSyncTime) {
			this.watchdogSyncTime = watchdogSyncTime;
			
			return this;
		}
		
		public Builder<T> watchdogTimeout(long watchdogTimeout) {
			this.watchdogTimeout = watchdogTimeout;
			
			return this;
		}
		
		public Builder<T> serverMode() {
			serverMode = true;

			return this;
		}
		
		public abstract T build();
	}

	public ActorSystemConfig(Builder<?> builder) {
		super();
		this.name = builder.name;
		this.debugUnhandled = builder.debugUnhandled;
		this.debugUndelivered = builder.debugUndelivered;
		this.queueSize = builder.queueSize;
		this.bufferQueueSize = builder.bufferQueueSize;
		this.parallelism = builder.parallelism;
		this.parallelismFactor = builder.parallelismFactor;
		this.throughput = builder.throughput;
		this.maxSpins = builder.maxSpins;
		this.highLoad = builder.highLoad;
		this.threadMode = builder.threadMode;
		this.sleepTime = builder.sleepTime;
		this.maxResourceThreads = builder.maxResourceThreads;
		this.awaitTerminationTimeout = builder.awaitTerminationTimeout;
		this.maxRetries = builder.maxRetries;
		this.withinTimeRange = builder.withinTimeRange;
		this.persistenceDriver = builder.persistenceDriver;
		this.persistenceMode = builder.persistenceMode;
		this.counterEnabled = new AtomicBoolean(builder.counterEnabled);
		this.metricsEnabled = new AtomicBoolean(builder.metricsEnabled);
		this.processingTimeEnabled = new AtomicBoolean(builder.processingTimeEnabled);
		this.maxProcessingTimeSamples = builder.maxProcessingTimeSamples;
		this.trackProcessingTimePerActor = new AtomicBoolean(builder.trackProcessingTimePerActor);
		this.trackRequestRatePerActor = new AtomicBoolean(builder.trackRequestRatePerActor);
		this.horizontalPodAutoscalerEnabled = builder.horizontalPodAutoscalerEnabled;
		this.horizontalPodAutoscalerSyncTime = builder.horizontalPodAutoscalerSyncTime;
		this.horizontalPodAutoscalerMeasurementTime = builder.horizontalPodAutoscalerMeasurementTime;
		this.podCaching = builder.podCaching;
		this.podDatabase = builder.podDatabase;
		this.podHost = builder.podHost;
		this.watchdogEnabled = builder.watchdogEnabled;
		this.watchdogSyncTime = builder.watchdogSyncTime;
		this.watchdogTimeout = builder.watchdogTimeout;
		this.serverMode = builder.serverMode;
	}
	
	public static ActorSystemConfig create() {
		return new ActorSystemConfig(builder());
	}
	
	public static Builder<?> builder() {
		return new Builder<ActorSystemConfig>() {
			@Override
			public ActorSystemConfig build() {
				return new ActorSystemConfig(this);
			}
		};
	}
	
	public static Builder<?> builder(ActorSystemConfig config) {
		return new Builder<ActorSystemConfig>(config) {
			@Override
			public ActorSystemConfig build() {
				return new ActorSystemConfig(this);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public <T> T podCacheManager() {
		if (podCaching!=null)
			return (T) podCaching.getCacheManager();
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T podDatabaseClient() {
		if (podDatabase!=null)
			return (T) podDatabase.getClient();
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T podHostInstance() {
		if (podHost!=null)
			return (T) podHost.getInstance();
		else
			return null;
	}
}
