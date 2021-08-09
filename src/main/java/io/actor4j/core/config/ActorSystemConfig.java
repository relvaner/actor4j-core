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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.actor4j.core.ActorClientRunnable;
import io.actor4j.core.ActorServiceNode;
import io.actor4j.core.internal.ActorThreadMode;
import io.actor4j.core.persistence.drivers.PersistenceDriver;
import io.actor4j.core.pods.Database;

public class ActorSystemConfig {
	public final String name;

	public final AtomicBoolean counterEnabled;
	public final AtomicBoolean threadProcessingTimeEnabled;

	public final int parallelism;
	public final int parallelismFactor;

	public final int idle;
	public final int load;
	public final ActorThreadMode threadMode;
	public final long sleepTime;
	public final long horizontalPodAutoscalerSyncTime;
	public final long horizontalPodAutoscalerMeasurementTime;

	public final int maxStatisticValues;

	public final boolean debugUnhandled;

	public final int queueSize;
	public final int bufferQueueSize;

	public final int throughput;

	public final PersistenceDriver persistenceDriver;
	public final boolean persistenceMode;

	public final Database<?> podDatabase;

	public final String serviceNodeName;
	public final List<ActorServiceNode> serviceNodes;
	public final boolean clientMode;
	public final boolean serverMode;
	public final ActorClientRunnable clientRunnable;

	public static abstract class Builder<T extends ActorSystemConfig> {
		protected String name;

		protected boolean counterEnabled;
		protected boolean threadProcessingTimeEnabled;

		protected int parallelism;
		protected int parallelismFactor;

		protected int idle;
		protected int load;
		protected ActorThreadMode threadMode;
		protected long sleepTime;
		protected long horizontalPodAutoscalerSyncTime;
		protected long horizontalPodAutoscalerMeasurementTime;

		protected int maxStatisticValues;

		protected boolean debugUnhandled;

		protected int queueSize;
		protected int bufferQueueSize;

		protected int throughput;

		protected PersistenceDriver persistenceDriver;
		protected boolean persistenceMode;

		protected Database<?> podDatabase;

		protected String serviceNodeName;
		protected List<ActorServiceNode> serviceNodes;
		protected boolean clientMode;
		protected boolean serverMode;
		protected ActorClientRunnable clientRunnable;

		public Builder() {
			super();

			name = "actor4j";

			counterEnabled = false;
			threadProcessingTimeEnabled = false;

			parallelism(0);
			parallelismFactor = 1;

			throughput = 100;

			idle = 100_000;
			calculateLoad();
			threadMode = ActorThreadMode.PARK;
			sleepTime = 25;
			horizontalPodAutoscalerSyncTime = 15_000;
			horizontalPodAutoscalerMeasurementTime = 2_000;

			maxStatisticValues = 10_000;

			queueSize = 50_000;
			bufferQueueSize = 10_000;

			persistenceMode = false;

			serviceNodeName = "Default Node";
			serviceNodes = new ArrayList<>();
		}

		public Builder<T> name(String name) {
			if (name!=null)
				this.name = name;
			else
				this.name = "actor4j";

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
		
		public Builder<T> horizontalPodAutoscalerSyncTime(long horizontalPodAutoscalerSyncTime) {
			this.horizontalPodAutoscalerSyncTime = horizontalPodAutoscalerSyncTime;
			
			return this;
		}

		public Builder<T> horizontalPodAutoscalerMeasurementTime(long horizontalPodAutoscalerMeasurementTime) {
			this.horizontalPodAutoscalerMeasurementTime = horizontalPodAutoscalerMeasurementTime;
			
			return this;
		}

		public Builder<T> maxStatisticValues(int maxStatisticValues) {
			this.maxStatisticValues = maxStatisticValues;
			
			return this;
		}

		public Builder<T> debugUnhandled(boolean debugUnhandled) {
			this.debugUnhandled = debugUnhandled;

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

		public Builder<T> throughput(int throughput) {
			this.throughput = throughput;
			calculateLoad();

			return this;
		}
		
		public Builder<T> persistenceMode(PersistenceDriver persistenceDriver) {
			this.persistenceDriver = persistenceDriver;
			this.persistenceMode = true;

			return this;
		}

		public Builder<T> podDatabase(Database<?> podDatabase) {
			this.podDatabase = podDatabase;

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
		this.counterEnabled = new AtomicBoolean(builder.counterEnabled);
		this.threadProcessingTimeEnabled = new AtomicBoolean(builder.threadProcessingTimeEnabled);
		this.parallelism = builder.parallelism;
		this.parallelismFactor = builder.parallelismFactor;
		this.idle = builder.idle;
		this.load = builder.load;
		this.threadMode = builder.threadMode;
		this.sleepTime = builder.sleepTime;
		this.horizontalPodAutoscalerSyncTime = builder.horizontalPodAutoscalerSyncTime;
		this.horizontalPodAutoscalerMeasurementTime = builder.horizontalPodAutoscalerMeasurementTime;
		this.maxStatisticValues = builder.maxStatisticValues;
		this.debugUnhandled = builder.debugUnhandled;
		this.queueSize = builder.queueSize;
		this.bufferQueueSize = builder.bufferQueueSize;
		this.throughput = builder.throughput;
		this.persistenceDriver = builder.persistenceDriver;
		this.persistenceMode = builder.persistenceMode;
		this.podDatabase = builder.podDatabase;
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

	@SuppressWarnings("unchecked")
	public <T> T getPodDatabase() {
		if (podDatabase!=null)
			return (T) podDatabase.getClient();
		else
			return null;
	}
}
