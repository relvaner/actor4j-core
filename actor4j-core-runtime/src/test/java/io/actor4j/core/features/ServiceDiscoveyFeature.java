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
package io.actor4j.core.features;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.immutable.ImmutableObject;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.service.discovery.Service;
import io.actor4j.core.service.discovery.ServiceDiscoveryManager;

public class ServiceDiscoveyFeature {
	@Test(timeout=5000)
	public void test_discovery() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorSystem system = ActorSystem.create();
		
		system.addActor(() -> new Actor("parent") {
			protected ServiceDiscoveryManager serviceDiscoveryManager;
			protected UUID child1;
			protected UUID child2;
			
			@Override
			public void preStart() {
				serviceDiscoveryManager = new ServiceDiscoveryManager(this, "serviceDiscovery");
				system.addActor(ServiceDiscoveryManager.create("serviceDiscovery"));
				
				child1 = addChild(() -> new Actor("child1") {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				child2 = addChild(() -> new Actor("child2") {
					@Override
					public void receive(ActorMessage<?> message) {
					}
				});
				
				serviceDiscoveryManager.publish(new Service("child1", getSystem().getActorPath(child1), Arrays.asList("childTopicA", "childTopicB"), "1.0.0", "description"));
				serviceDiscoveryManager.publish(new Service("child2", getSystem().getActorPath(child2), Arrays.asList("childTopicA", "childTopicC"), "1.0.0", "description"));
				serviceDiscoveryManager.lookupFirst("childTopicB");
				serviceDiscoveryManager.lookup("childTopicA");
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				Optional<ImmutableObject<Service>> optional = serviceDiscoveryManager.lookupFirst(message);
				if (optional.isPresent() && optional.get().get()!=null) {
					assertEquals("child1", optional.get().get().name);
					assertTrue(optional.get().get().topics.contains("childTopicB"));
					become((msg) -> {
						Optional<ImmutableList<Service>> optional2 = serviceDiscoveryManager.lookup(msg);
						if (optional2.isPresent() && !optional2.get().get().isEmpty()) {
							Map<String, Boolean> map = new HashMap<>();
							map.put("child1", false);
							map.put("child2", false);
							optional2.get().get().stream().map((s) -> s.name).forEach((name) -> map.put(name, true));
							assertTrue(map.get("child1"));
							assertTrue(map.get("child2"));
							assertTrue(optional2.get().get().stream().map((s) -> s.topics.contains("childTopicA")).reduce((t1, t2) -> t1 && t2).get());
							unbecome();
							
							testDone.countDown();
						}
					});
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
