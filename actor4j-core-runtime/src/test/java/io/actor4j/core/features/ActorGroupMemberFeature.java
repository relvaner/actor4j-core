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
package io.actor4j.core.features;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.ActorWithBothGroups;
import io.actor4j.core.actors.ActorWithDistributedGroup;
import io.actor4j.core.actors.ActorWithGroup;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;

import static io.actor4j.core.logging.ActorLogger.*;
import static org.junit.Assert.*;

public class ActorGroupMemberFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		ActorSystemConfig config = ActorSystemConfig.builder()
			.parallelism(3) /* temporary solution */
			.build();
		system = ActorSystem.create(config);
	}
	
	@Test(timeout=30000)
	public void test_ActorGroupMember() {
		int instances = system.getConfig().parallelism()+1;
		CountDownLatch testDone = new CountDownLatch(instances*2);
		AtomicReference<String> threadName1 = new AtomicReference<>("");
		AtomicReference<String> threadName2 = new AtomicReference<>("");
		
		ActorGroup group1 = new ActorGroupSet();
		system.setAlias(system.addActor(() -> new ActorWithGroup(group1) {
			protected boolean first = true;
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("from thread %s of actor %s", Thread.currentThread().getName(), self()));
				if (first) {
					if (!threadName1.compareAndSet("", Thread.currentThread().getName()))
						assertEquals(threadName1.get(), Thread.currentThread().getName());
					testDone.countDown();
					first=false;
				}
			}
		}, instances), "instances");
		ActorGroup group2 = new ActorGroupSet();
		system.setAlias(system.addActor(() -> new ActorWithGroup(group2) {
			protected boolean first = true;
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("from thread %s of actor %s", Thread.currentThread().getName(), self()));
				if (first) {
					if (!threadName2.compareAndSet("", Thread.currentThread().getName()))
						assertEquals(threadName2.get(), Thread.currentThread().getName());
					testDone.countDown();
					first=false;
				}
			}
		}, instances), "instances");
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				system.sendViaAlias(ActorMessage.create(null, 0, system.SYSTEM_ID(), null), "instances");
			}
		}, 0, 50);
		
		system.start();
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=30000)
	public void test_ActorDistributedGroupMember() {
		int instances = system.getConfig().parallelism();
		CountDownLatch testDone = new CountDownLatch(instances);
		Map<String, Boolean> map = new ConcurrentHashMap<String, Boolean>();
		
		ActorGroup group1 = new ActorGroupSet();
		system.setAlias(system.addActor(() -> new ActorWithDistributedGroup(group1) {
			protected boolean first = true;
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("from thread %s of actor %s", Thread.currentThread().getName(), self()));
				if (first) {
					if (map.get(Thread.currentThread().getName())==null)
						map.put(Thread.currentThread().getName(), true);
					testDone.countDown();
					first=false;
				}
			}
		}, instances), "instances");
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				system.sendViaAlias(ActorMessage.create(null, 0, system.SYSTEM_ID(), null), "instances");
			}
		}, 0, 50);
		
		system.start();
				
		try {
			testDone.await();
			assertEquals(instances, map.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=30000)
	public void test_ActorWithBothGroups_with_ActorWithGroup() {
		int instances = system.getConfig().parallelism();
		CountDownLatch testDone = new CountDownLatch(instances+instances*instances);
		Map<String, Boolean> map = new ConcurrentHashMap<String, Boolean>();
		Map<UUID, String> threadMap = new ConcurrentHashMap<UUID, String>();
		
		ActorGroup distributedGroup = new ActorGroupSet();
		system.setAlias(system.addActor(() -> new ActorWithBothGroups(distributedGroup) {
			protected ActorGroup group = new ActorGroupSet();
			protected boolean first = true;
			@Override
			public void preStart() {
				system.setAlias(system.addActor(() -> new ActorWithGroup(group) {
					protected boolean first_child = true;
					@Override
					public void receive(ActorMessage<?> message) {
						logger().log(DEBUG, String.format("from thread %s of actor %s", Thread.currentThread().getName(), self()));
						if (first_child) {
							if (!threadMap.containsKey(groupId))
								threadMap.put(groupId, Thread.currentThread().getName());
							else
								assertEquals(threadMap.get(groupId), Thread.currentThread().getName());
							testDone.countDown();
							first_child=false;
						}
					}
				}, instances), "instances_child");
			}
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("from thread %s of actor %s", Thread.currentThread().getName(), self()));
				if (first) {
					if (map.get(Thread.currentThread().getName())==null)
						map.put(Thread.currentThread().getName(), true);
					testDone.countDown();
					first=false;
					
					if (!threadMap.containsKey(getGroupId()))
						threadMap.put(getGroupId(), Thread.currentThread().getName());
					else
						assertEquals(threadMap.get(getGroupId()), Thread.currentThread().getName());
				}
			}
			@Override
			public UUID getGroupId() {
				return group.getId();
			}
		}, instances), "instances");

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				system.sendViaAlias(ActorMessage.create(null, 0, system.SYSTEM_ID(), null), "instances");
				system.sendViaAlias(ActorMessage.create(null, 0, system.SYSTEM_ID(), null), "instances_child");
			}
		}, 0, 50);
		
		system.start();

		try {
			testDone.await();
			assertEquals(instances, map.size());
			assertEquals(instances, threadMap.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();
		system.shutdownWithActors(true);
	}
}
