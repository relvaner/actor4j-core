/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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
package actor4j.core.features;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.immutable.ImmutableObject;
import actor4j.core.messages.ActorMessage;
import actor4j.core.service.discovery.Service;
import actor4j.core.service.discovery.ServiceDiscoveryManager;

public class ServiceDiscoveyFeature {
	@Test(timeout=5000)
	public void test_discovery() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorSystem system = new ActorSystem();
		
		system.addActor(() -> new Actor("parent") {
			protected ServiceDiscoveryManager serviceDiscoveryManager;
			protected UUID child;
			
			@Override
			public void preStart() {
				serviceDiscoveryManager = new ServiceDiscoveryManager(this, "serviceDiscovery");
				system.addActor(ServiceDiscoveryManager.create("serviceDiscovery"));
				
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				
				serviceDiscoveryManager.publish(new Service("child", getSystem().getActorPath(child), Arrays.asList("childTopicA", "childTopicB"), "1.0.0", "description"));
				serviceDiscoveryManager.lookupFirst("childTopicB");
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				Optional<ImmutableObject<Service>> optional = serviceDiscoveryManager.lookupFirst(message);
				if (optional.isPresent() && optional.get().get()!=null) {
					testDone.countDown();
					assertEquals("child", optional.get().get().getName());
					assertTrue(optional.get().get().getTopics().contains("childTopicB"));
				}
			}
		});
		
		system.start();
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
