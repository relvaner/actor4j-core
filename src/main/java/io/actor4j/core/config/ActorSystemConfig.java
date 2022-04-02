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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.ActorClientRunnable;
import io.actor4j.core.ActorServiceNode;
import io.actor4j.core.internal.ActorThreadMode;
import io.actor4j.core.persistence.drivers.PersistenceDriver;
import io.actor4j.core.pods.Database;

public class ActorSystemConfig {
	private final String name;
	
	private final boolean debugUnhandled;
	private final boolean debugUndelivered;

	private final int parallelism;
	private final int parallelismFactor;
	
	private final int queueSize;
	private final int bufferQueueSize;

	private final int throughput;
	private final int idle;
	private final int load;
	private final ActorThreadMode threadMode;
	private final long sleepTime;
	
	private final int maxResourceThreads;
	
	// Supervisor
	private final int maxRetries;
	private final long withinTimeRange;
	
	// Persistence
	private final PersistenceDriver persistenceDriver;
	private final boolean persistenceMode;
	
	// Metrics
	private final AtomicBoolean counterEnabled;
	private final AtomicBoolean threadProcessingTimeEnabled;
	private final int maxStatisticValues;
	
	// Pods
	private final long horizontalPodAutoscalerSyncTime;
	private final long horizontalPodAutoscalerMeasurementTime;
	private final Database<?> podDatabase;
	
	// Watchdog
	private final long watchdogSyncTime;
	private final long watchdogTimeout;
	
	// As Service
	private final String serviceNodeName;
	private final List<ActorServiceNode> serviceNodes;
	private final boolean serverMode;
	private final boolean clientMode;
	private final ActorClientRunnable clientRunnable;
	
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
	
	public int idle() {
		return idle;
	}
	
	public int load() {
		return load;
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
	
	public AtomicBoolean threadProcessingTimeEnabled() {
		return threadProcessingTimeEnabled;
	}
	
	public int maxStatisticValues() {
		return maxStatisticValues;
	}
	
	public long horizontalPodAutoscalerSyncTime() {
		return horizontalPodAutoscalerSyncTime;
	}
	
	public long horizontalPodAutoscalerMeasurementTime() {
		return horizontalPodAutoscalerMeasurementTime;
	}
	
	public Database<?> podDatabase() {
		return podDatabase;
	}
	
	public long watchdogSyncTime() {
		return watchdogSyncTime;
	}
	
	public long watchdogTimeout() {
		return watchdogTimeout;
	}
	
	public String serviceNodeName() {
		return serviceNodeName;
	}
	
	public List<ActorServiceNode> serviceNodes() {
		return serviceNodes;
	}
	
	public boolean serverMode() {
		return serverMode;
	}
	
	public boolean clientMode() {
		return clientMode;
	}
	
	public ActorClientRunnable clientRunnable() {
		return clientRunnable;
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
		protected int idle;
		protected int load;
		protected ActorThreadMode threadMode;
		protected long sleepTime;
		
		protected int maxResourceThreads;
		
		// Supervisor
		protected int maxRetries;
		protected long withinTimeRange;
		
		// Persistence
		protected PersistenceDriver persistenceDriver;
		protected boolean persistenceMode;

		// Metrics
		protected boolean counterEnabled;
		protected boolean threadProcessingTimeEnabled;
		protected int maxStatisticValues;
		
		// Pods
		protected long horizontalPodAutoscalerSyncTime;
		protected long horizontalPodAutoscalerMeasurementTime;
		protected Database<?> podDatabase;
		
		// Watchdog
		protected long watchdogSyncTime;
		protected long watchdogTimeout;
		
		// As Service
		protected String serviceNodeName;
		protected List<ActorServiceNode> serviceNodes;
		protected boolean clientMode;
		protected boolean serverMode;
		protected ActorClientRunnable clientRunnable;

		public Builder() {
			super();

			name = "actor4j";
			
			parallelism(0);
			parallelismFactor = 1;
			
			queueSize = 50_000;
			bufferQueueSize = 10_000;
			
			throughput = 100;
			idle = 100_000;
			calculateLoad();
			threadMode = ActorThreadMode.PARK;
			sleepTime = 25;
			
			maxResourceThreads = 200;
			
			// Supervisor
			maxRetries = 3;
			withinTimeRange = 2_000;
			
			// Persistence
			persistenceMode = false;

			// Metrics
			counterEnabled = false;
			threadProcessingTimeEnabled = false;
			maxStatisticValues = 10_000;

			// Pods
			horizontalPodAutoscalerSyncTime = 15_000;
			horizontalPodAutoscalerMeasurementTime = 2_000;
			
			// Watchdog
			watchdogSyncTime = 5_000;
			watchdogTimeout = 2_000;

			// As Service
			serviceNodeName = "Default Node";
			serviceNodes = new LinkedList<>();
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
			this.idle = config.idle();
			this.load = config.load();
			this.threadMode = config.threadMode();
			this.sleepTime = config.sleepTime();
			this.maxResourceThreads = config.maxResourceThreads();
			this.maxRetries = config.maxRetries();
			this.withinTimeRange = config.withinTimeRange();
			this.persistenceDriver = config.persistenceDriver();
			this.persistenceMode = config.persistenceMode();
			this.counterEnabled = config.counterEnabled().get();
			this.threadProcessingTimeEnabled = config.threadProcessingTimeEnabled().get();
			this.maxStatisticValues = config.maxStatisticValues();
			this.horizontalPodAutoscalerSyncTime = config.horizontalPodAutoscalerSyncTime();
			this.horizontalPodAutoscalerMeasurementTime = config.horizontalPodAutoscalerMeasurementTime();
			this.podDatabase = config.podDatabase();
			this.watchdogSyncTime = config.watchdogSyncTime();
			this.watchdogTimeout = config.watchdogTimeout();
			this.serviceNodeName = config.serviceNodeName();
			this.serviceNodes = new LinkedList<>(config.serviceNodes());
			this.clientMode = config.clientMode();
			this.serverMode = config.serverMode();
			this.clientRunnable = config.clientRunnable();
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
			calculateLoad();

			return this;
		}
		
		public Builder<T> idle(int idle) {
			this.idle = idle;
			calculateLoad();

			return this;
		}
		
		protected void calculateLoad() {
			load = idle / throughput;
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
		
		public Builder<T> maxResourceThreads(int maxResourceThreads) {
			this.maxResourceThreads = maxResourceThreads;

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

		public Builder<T> threadProcessingTimeEnabled(boolean enabled) {
			threadProcessingTimeEnabled = enabled;

			return this;
		}
		
		public Builder<T> maxStatisticValues(int maxStatisticValues) {
			this.maxStatisticValues = maxStatisticValues;
			
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

		public Builder<T> podDatabase(Database<?> podDatabase) {
			this.podDatabase = podDatabase;

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

		public Builder<T> serviceNodeName(String serviceNodeName) {
			this.serviceNodeName = serviceNodeName;

			return this;
		}

		public Builder<T> addServiceNode(ActorServiceNode serviceNode) {
			serviceNodes.add(serviceNode);

			return this;
		}
		
		public Builder<T> serverMode() {
			serverMode = true;

			return this;
		}

		public Builder<T> clientRunnable(ActorClientRunnable clientRunnable) {
			clientMode = (clientRunnable != null);

			this.clientRunnable = clientRunnable;

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
		this.idle = builder.idle;
		this.load = builder.load;
		this.threadMode = builder.threadMode;
		this.sleepTime = builder.sleepTime;
		this.maxResourceThreads = builder.maxResourceThreads;
		this.maxRetries = builder.maxRetries;
		this.withinTimeRange = builder.withinTimeRange;
		this.persistenceDriver = builder.persistenceDriver;
		this.persistenceMode = builder.persistenceMode;
		this.counterEnabled = new AtomicBoolean(builder.counterEnabled);
		this.threadProcessingTimeEnabled = new AtomicBoolean(builder.threadProcessingTimeEnabled);
		this.maxStatisticValues = builder.maxStatisticValues;
		this.horizontalPodAutoscalerSyncTime = builder.horizontalPodAutoscalerSyncTime;
		this.horizontalPodAutoscalerMeasurementTime = builder.horizontalPodAutoscalerMeasurementTime;
		this.podDatabase = builder.podDatabase;
		this.watchdogSyncTime = builder.watchdogSyncTime;
		this.watchdogTimeout = builder.watchdogTimeout;
		this.serviceNodeName = builder.serviceNodeName;
		this.serviceNodes = Collections.unmodifiableList(builder.serviceNodes);
		this.clientMode = builder.clientMode;
		this.serverMode = builder.serverMode;
		this.clientRunnable = builder.clientRunnable;
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
	public <T> T getPodDatabase() {
		if (podDatabase!=null)
			return (T) podDatabase.getClient();
		else
			return null;
	}
}
