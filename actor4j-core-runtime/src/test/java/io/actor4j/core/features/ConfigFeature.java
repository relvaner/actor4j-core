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
package io.actor4j.core.features;

import static org.junit.Assert.*;

import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.internal.ActorThreadMode;

public class ConfigFeature {
	@Test
	public void test_default() {
		ActorSystem system = ActorSystem.create();
		
		assertEquals("actor4j", system.getConfig().name());
		assertEquals(false, system.getConfig().counterEnabled().get());
		assertEquals(false, system.getConfig().threadProcessingTimeEnabled().get());
		assertEquals(Runtime.getRuntime().availableProcessors(), system.getConfig().parallelism());
		assertEquals(1, system.getConfig().parallelismFactor());
		assertEquals(100, system.getConfig().throughput());
		assertEquals(100_000, system.getConfig().idle());
		assertEquals(100_000/100, system.getConfig().load());
		assertEquals(ActorThreadMode.PARK, system.getConfig().threadMode());
		assertEquals(25, system.getConfig().sleepTime());
		assertEquals(15_000, system.getConfig().horizontalPodAutoscalerSyncTime());
		assertEquals(2_000, system.getConfig().horizontalPodAutoscalerMeasurementTime());
		assertEquals(10_000, system.getConfig().maxStatisticValues());
		assertEquals(50_000, system.getConfig().queueSize());
		assertEquals(10_000, system.getConfig().bufferQueueSize());
		assertEquals(false, system.getConfig().persistenceMode());
		assertEquals("Default Node", system.getConfig().serviceNodeName());
		
		assertEquals(null, system.getConfig().persistenceDriver());
		assertEquals(null, system.getConfig().podDatabase());
		assertEquals(0, system.getConfig().serviceNodes().size());
		assertEquals(false, system.getConfig().clientMode());
		assertEquals(false, system.getConfig().serverMode());
		assertEquals(null, system.getConfig().clientRunnable());
	}
	
	@Test
	public void test() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.name("test")
			.counterEnabled(true)
			.threadProcessingTimeEnabled(true)
			.parallelism(3)
			.parallelismFactor(2)
			.throughput(200)
			.idle(10_000)
			.sleepMode(100)
			.horizontalPodAutoscalerSyncTime(30_000)
			.horizontalPodAutoscalerMeasurementTime(3_000)
			.maxStatisticValues(12_000)
			.queueSize(55_000)
			.bufferQueueSize(13_000)
			.serviceNodeName("test-node")
			.serverMode()
			.build();
		ActorSystem system = ActorSystem.create(config);
		
		assertEquals("test", system.getConfig().name());
		assertEquals(true, system.getConfig().counterEnabled().get());
		assertEquals(true, system.getConfig().threadProcessingTimeEnabled().get());
		assertEquals(3, system.getConfig().parallelism());
		assertEquals(2, system.getConfig().parallelismFactor());
		assertEquals(200, system.getConfig().throughput());
		assertEquals(10_000, system.getConfig().idle());
		assertEquals(10_000/200, system.getConfig().load());
		assertEquals(ActorThreadMode.SLEEP, system.getConfig().threadMode());
		assertEquals(100, system.getConfig().sleepTime());
		assertEquals(30_000, system.getConfig().horizontalPodAutoscalerSyncTime());
		assertEquals(3_000, system.getConfig().horizontalPodAutoscalerMeasurementTime());
		assertEquals(12_000, system.getConfig().maxStatisticValues());
		assertEquals(55_000, system.getConfig().queueSize());
		assertEquals(13_000, system.getConfig().bufferQueueSize());
		assertEquals(false, system.getConfig().persistenceMode());
		assertEquals("test-node", system.getConfig().serviceNodeName());
		
		assertEquals(null, system.getConfig().persistenceDriver());
		assertEquals(null, system.getConfig().podDatabase());
		assertEquals(0, system.getConfig().serviceNodes().size());
		assertEquals(false, system.getConfig().clientMode());
		assertEquals(true, system.getConfig().serverMode());
		assertEquals(null, system.getConfig().clientRunnable());
	}
}
