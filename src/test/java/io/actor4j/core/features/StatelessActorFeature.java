/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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

import static io.actor4j.core.logging.user.ActorLogger.logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.StatelessActor;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;

public class StatelessActorFeature {
	protected ActorSystem system;
	
	@Before
	public void before() {
		system = new ActorSystem("StatelessActorFeature");
	}
	
	@Test(timeout=30000)
	public void test() {
		system.setParallelismFactor(2);
		
		CountDownLatch testDone = new CountDownLatch(system.getParallelismMin()*system.getParallelismFactor());
		
		ActorGroup group = new ActorGroupSet();
		system.setAlias(system.addActor(() -> new StatelessActor(group) {
			protected boolean first = true;
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("from thread %s of actor %s", Thread.currentThread().getName(), self()));
				if (first) {
					testDone.countDown();
					first=false;
				}
			}
		}, system.getParallelismMin()*system.getParallelismFactor()), "instances");
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				system.sendViaAlias(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, null), "instances");
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
}
