/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.server.rest;

import java.util.UUID;

import actor4j.core.ActorServiceNode;
import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.server.rest.RESTActorClientRunnable;

public class ClientApplication {
	public ClientApplication() {
		ActorSystem system = new ActorSystem();
		configure(system);
		system.setClientRunnable(new RESTActorClientRunnable(system.getServiceNodes(), system.getParallelismMin()*system.getParallelismFactor(), 10000));
		system.start();
		
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		system.shutdown();
	}
	
	protected void configure(ActorSystem system) {
		system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		system.addServiceNode(new ActorServiceNode("Node 1", "http://localhost:8080/actor4j-benchmark-server/api"));
		
		UUID client = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Client("server");
			}
		});
		Payload payload = new Payload();
		payload.data = "";
		
		system.timer().schedule(new ActorMessage<Payload>(payload, 200, system.SYSTEM_ID, null), client, 1000, 1000);
	}
	
	public static void main(String[] args) {
		new ClientApplication();
	}
}
