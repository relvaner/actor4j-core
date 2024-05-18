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
import io.actor4j.core.features.pod.ExampleReplicationWithRemoteActorPodWithRequest;
import io.actor4j.core.features.pod.ExampleReplicationWithRemoteFunctionPod;
import io.actor4j.core.features.pod.ExampleShardingWithActorPod;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodConfiguration;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.RemotePodMessageDTO;
import io.actor4j.core.pods.utils.PodRequestMethod;
import io.actor4j.core.runtime.ActorGlobalSettings;
import io.actor4j.core.runtime.InternalActorSystem;
import io.actor4j.core.utils.ActorGroupSet;

import static io.actor4j.core.logging.ActorLogger.*;
import static org.junit.Assert.*;

public class PodFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		system = ActorSystem.create(AllFeaturesTest.factory());
	}
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithActorPod() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithActorPod(), 
				new PodConfiguration("ExampleReplicationWithActorPod", ExampleReplicationWithActorPod.class.getName(), 1, 1));
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		system.sendViaAlias(ActorMessage.create("Test", 0, client, null), "ExampleReplicationWithActorPod");
		
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
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source());
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = ((InternalActorSystem)system).getActorsFromAlias("ExampleReplicationWithActorPod");
				system.broadcast(ActorMessage.create("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), starter));
		
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
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source());
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = ((InternalActorSystem)system).getActorsFromAlias("ExampleReplicationWithActorPod");
				system.broadcast(ActorMessage.create("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), starter));
		
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
				Queue<UUID> queue = ((InternalActorSystem)system).getPodDomains().get("ExampleReplicationWithActorPod");
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
				if (message.tag()==TERMINATED)
					pods_postcondition.add(message.source());
				
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
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithRemoteActorPodWithRequest() {
		CountDownLatch testDone = new CountDownLatch(2);
		
		system.deployPods(
				() -> new ExampleReplicationWithRemoteActorPodWithRequest(), 
				new PodConfiguration("ExampleReplicationWithRemoteActorPodWithRequest", ExampleReplicationWithRemoteActorPodWithRequest.class.getName(), 1, 1));
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().equals("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		
		ActorGlobalSettings.internal_server_request = (msg, tag, source, interaction, params, domain) -> {
			if (interaction!=null) {
				RemotePodMessage remotePodMessage = new RemotePodMessage(new RemotePodMessageDTO("Hello "+msg.toString()+"!", PodRequestMethod.ACTION_1, "ExampleReplicationWithRemoteActorPodWithRequest", false), client.toString(), null);
				system.sendViaAlias(ActorMessage.create(remotePodMessage, 0, system.SYSTEM_ID(), null, interaction), "ExampleReplicationWithRemoteActorPodWithRequest");
			}
			else {
				assertTrue(msg.toString().equals("Test Moin!"));
				testDone.countDown();
			}
		};
		system.sendViaAlias(ActorMessage.create("Test Moin!", PodRequestMethod.ACTION_1, client, null), "ExampleReplicationWithRemoteActorPodWithRequest");
		system.sendViaAlias(ActorMessage.create("Test", PodRequestMethod.ACTION_2, client, null, client), "ExampleReplicationWithRemoteActorPodWithRequest");
		
		try {
			testDone.await();
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
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithFunctionPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		
		system.sendViaAlias(ActorMessage.create("Test", 0, client, null), "ExampleReplicationWithFunctionPod");
		
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
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithFunctionPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source());
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = ((InternalActorSystem)system).getActorsFromAlias("ExampleReplicationWithFunctionPod");
				system.broadcast(ActorMessage.create("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_factory_ExampleReplicationWithRemoteFunctionPod_and_ExampleReplicationWithFunctionPod() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleReplicationWithFunctionPod(), 
				new PodConfiguration("ExampleReplicationWithFunctionPod", ExampleReplicationWithFunctionPod.class.getName(), 1, 1));
		system.deployPods(
				() -> new ExampleReplicationWithRemoteFunctionPod(), 
				new PodConfiguration("ExampleReplicationWithRemoteFunctionPod", ExampleReplicationWithRemoteFunctionPod.class.getName(), 1, 1));
		
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleReplicationWithRemoteFunctionPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		
		ActorGlobalSettings.internal_server_callback = (replyAddress, result, tag) -> system.send(ActorMessage.create(result, tag, system.SYSTEM_ID(), UUID.fromString(replyAddress)));
		
		RemotePodMessage remotePodMessage = new RemotePodMessage(new RemotePodMessageDTO("Test", 0, "ExampleReplicationWithRemoteFunctionPod", true), client.toString(), null);
		system.sendViaAlias(ActorMessage.create(remotePodMessage, 0, system.SYSTEM_ID(), null), "ExampleReplicationWithRemoteFunctionPod");
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	//----------------------------------------------------------------------------------------------------------------------------------------------
	
	@Test(timeout=5000)
	public void test_factory_ExampleShardingWithActorPod() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleShardingWithActorPod(), 
				new PodConfiguration("ExampleShardingWithActorPod", ExampleShardingWithActorPod.class.getName(), 1, 1, 1));
		UUID client = system.addActor(() -> new Actor(){
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleShardingWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				testDone.countDown();
			}
		});
		system.start();
		
		system.sendViaAlias(ActorMessage.create("Test", 0, client, null), "ExampleShardingWithActorPod");
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}
	
	@Test(timeout=5000)
	public void test_factory_ExampleShardingWithActorPod_more() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		system.deployPods(
				() -> new ExampleShardingWithActorPod(), 
				new PodConfiguration("ExampleShardingWithActorPod", ExampleShardingWithActorPod.class.getName(), 3, 1, 1));
		
		UUID client = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleShardingWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				handlers.add(message.source());
				if (handlers.size()==1)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = ((InternalActorSystem)system).getActorsFromAlias("ExampleShardingWithActorPod");
				assertEquals(3, handlers.size());
				system.broadcast(ActorMessage.create("Test", 0, client, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}	
		
	@Test(timeout=5000)
	public void test_factory_ExampleShardingWithActorPod_more_replicas() {
		CountDownLatch testDone = new CountDownLatch(3);
		
		system.deployPods(
				() -> new ExampleShardingWithActorPod(), 
				new PodConfiguration("ExampleShardingWithActorPod", ExampleShardingWithActorPod.class.getName(), 3, 3, 3));
		
		UUID clientA = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleShardingWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Test!"));
				assertTrue(message.valueAsString().contains("shardId:2"));
				handlers.add(message.source());
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID clientB = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleShardingWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello Zzzz!"));
				assertTrue(message.valueAsString().contains("shardId:0"));
				handlers.add(message.source());
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID clientC = system.addActor(() -> new Actor() {
			protected Set<UUID> handlers;
			
			@Override
			public void preStart() {
				handlers = new HashSet<>();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				logger().log(DEBUG, String.format("client received a message ('%s') from ExampleShardingWithActorPod", message.value()));
				
				assertEquals(42, message.tag());
				assertTrue(message.value()!=null);
				assertTrue(message.value() instanceof String);
				assertTrue(message.valueAsString().startsWith("Hello aaaa!"));
				assertTrue(message.valueAsString().contains("shardId:1"));
				handlers.add(message.source());
				if (handlers.size()==3)
					testDone.countDown();
			}
		});
		UUID starter = system.addActor(() -> new Actor() {
			@Override
			public void receive(ActorMessage<?> message) {
				List<UUID> handlers = ((InternalActorSystem)system).getActorsFromAlias("ExampleShardingWithActorPod");
				assertEquals(3*3, handlers.size());
				system.broadcast(ActorMessage.create("Test", 0, clientA, null), new ActorGroupSet(handlers));
				system.broadcast(ActorMessage.create("Zzzz", 0, clientB, null), new ActorGroupSet(handlers));
				system.broadcast(ActorMessage.create("aaaa", 0, clientC, null), new ActorGroupSet(handlers));
			}
		});
		system.start();
		system.send(ActorMessage.create(null, 0, system.SYSTEM_ID(), starter));
		
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdownWithActors(true);
	}	
}
