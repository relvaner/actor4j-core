/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong.bulk;

import static actor4j.benchmark.hub.ActorMessageTag.RUN;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class TestPingPong {
	public TestPingPong() {
		ActorSystem system = new ActorSystem();
		//system.setParallelismFactor(1);
		//system.setParallelismMin(1);
		system.softMode();
		
		ActorGroup group = new ActorGroup();
		int size = 1000;
		UUID dest = null;
		UUID id = null;
		for(int i=0; i<size; i++) {
			dest = system.addActor(Destination.class);
			id = system.addActor(Client.class, dest);
			group.add(id);
		}
		
		system.broadcast(new ActorMessage<Object>(new Object(), RUN, dest, null), group);
		
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestPingPong();
	}
}
