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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.features.pod.ExampleReplicationWithActorPod;
import io.actor4j.core.features.pod.ExampleReplicationWithFunctionPod;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.utils.ActorGroupSet;

import static io.actor4j.core.logging.user.ActorLogger.*;
import static org.junit.Assert.*;

public class PodFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		system = new ActorSystem();
	}
	
	@Test(timeout=5000)
	public void test_ExampleReplicationWithActorPod() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ClassLoader classLoader = getClass().getClassLoader();
		File jarFile = new File(classLoader.getResource("actor4j-examples-pods-1.0.0.jar").getFile());
		assertTrue(jarFile.exists());
		system.deployPods(
				jarFile, 
				new PodConfiguration("ExampleReplicationWithActorPod", "io.actor4j.examples.pods.replication.ExampleReplicationWithActorPod", 1, 1));
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		system.sendViaAlias(new ActorMessage<>("Test", 0, client, null), "ExampleReplicationWithActorPod");
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_ExampleReplicationWithActorPod_more() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ClassLoader classLoader = getClass().getClassLoader();
		File jarFile = new File(classLoader.getResource("actor4j-examples-pods-1.0.0.jar").getFile());
		assertTrue(jarFile.exists());
		system.deployPods(
				jarFile, 
				new PodConfiguration("ExampleReplicationWithActorPod", "io.actor4j.examples.pods.replication.ExampleReplicationWithActorPod", 3, 3));
		
		UUID client = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source);
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = system.underlyingImpl().getActorsFromAlias("ExampleReplicationWithActorPod");
				system.broadcast(new ActorMessage<>("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_ExampleReplicationWithActorPod_more_undeploy() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ClassLoader classLoader = getClass().getClassLoader();
		File jarFile = new File(classLoader.getResource("actor4j-examples-pods-1.0.0.jar").getFile());
		assertTrue(jarFile.exists());
		system.deployPods(
				jarFile, 
				new PodConfiguration("ExampleReplicationWithActorPod", "io.actor4j.examples.pods.replication.ExampleReplicationWithActorPod", 3, 3));
		
		UUID client = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source);
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = system.underlyingImpl().getActorsFromAlias("ExampleReplicationWithActorPod");
				system.broadcast(new ActorMessage<>("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		CountDownLatch testDone2 = new CountDownLatch(1);
		CountDownLatch testDone3 = new CountDownLatch(1);
		
		system.addActor(() -> new Actor() {
			protected Set<UUID> pods_precondition = new HashSet<>();
			protected Set<UUID> pods_postcondition = new HashSet<>();
			
			@Override
			public void preStart() {
				Queue<UUID> queue = system.underlyingImpl().getPodDomains().get("ExampleReplicationWithActorPod");
				Iterator<UUID> iterator = queue.iterator();
				while (iterator.hasNext()) {
					UUID pod = iterator.next();
					pods_precondition.add(pod);
					watch(pod);
				}
				testDone2.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag==TERMINATED)
					pods_postcondition.add(message.source);
				
				if (pods_postcondition.size()==3) {
					assertEquals(pods_precondition.size(), 3);
					assertEquals(pods_precondition, pods_postcondition);
					testDone3.countDown();
				}
			}
		});
		
		try {
			testDone2.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.undeployPods("ExampleReplicationWithActorPod");
		
		try {
			testDone3.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		
		system.shutdownWithActors(true);
	}
	
	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithActorPod() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithActorPod(), 
				new PodConfiguration("ExampleReplicationWithActorPod", ExampleReplicationWithActorPod.class.getName(), 1, 1));
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		system.sendViaAlias(new ActorMessage<>("Test", 0, client, null), "ExampleReplicationWithActorPod");
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
		
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithActorPod_more() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithActorPod(), 
				new PodConfiguration("ExampleReplicationWithActorPod", ExampleReplicationWithActorPod.class.getName(), 3, 3));
		
		UUID client = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source);
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = system.underlyingImpl().getActorsFromAlias("ExampleReplicationWithActorPod");
				system.broadcast(new ActorMessage<>("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithActorPod_more_undeploy() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithActorPod(), 
				new PodConfiguration("ExampleReplicationWithActorPod", ExampleReplicationWithActorPod.class.getName(), 3, 3));
		
		UUID client = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source);
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = system.underlyingImpl().getActorsFromAlias("ExampleReplicationWithActorPod");
				system.broadcast(new ActorMessage<>("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		CountDownLatch testDone2 = new CountDownLatch(1);
		CountDownLatch testDone3 = new CountDownLatch(1);
		
		system.addActor(() -> new Actor() {
			protected Set<UUID> pods_precondition = new HashSet<>();
			protected Set<UUID> pods_postcondition = new HashSet<>();
			
			@Override
			public void preStart() {
				Queue<UUID> queue = system.underlyingImpl().getPodDomains().get("ExampleReplicationWithActorPod");
				Iterator<UUID> iterator = queue.iterator();
				while (iterator.hasNext()) {
					UUID pod = iterator.next();
					pods_precondition.add(pod);
					watch(pod);
				}
				testDone2.countDown();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (message.tag==TERMINATED)
					pods_postcondition.add(message.source);
				
				if (pods_postcondition.size()==3) {
					assertEquals(pods_precondition.size(), 3);
					assertEquals(pods_precondition, pods_postcondition);
					testDone3.countDown();
				}
			}
		});
		
		try {
			testDone2.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.undeployPods("ExampleReplicationWithActorPod");
		
		try {
			testDone3.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		
		system.shutdownWithActors(true);
	}
	
	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithFunctionPod() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithFunctionPod(), 
				new PodConfiguration("ExampleReplicationWithFunctionPod", ExampleReplicationWithFunctionPod.class.getName(), 1, 1));
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithFunctionPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		
		system.sendViaAlias(new ActorMessage<>("Test", 0, client, null), "ExampleReplicationWithFunctionPod");
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithFunctionPod_more() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithFunctionPod(), 
				new PodConfiguration("ExampleReplicationWithFunctionPod", ExampleReplicationWithFunctionPod.class.getName(), 3, 3));
		
		UUID client = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().debug(String.format("client received a message ('%s') from ExampleReplicationWithFunctionPod", message.value));
				
				assertEquals(42, message.tag);
				assertTrue(message.value!=null);
				assertTrue(message.value instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source);
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = system.underlyingImpl().getActorsFromAlias("ExampleReplicationWithFunctionPod");
				system.broadcast(new ActorMessage<>("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
}
