/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ping.pong;

import static actor4j.benchmark.ping.pong.ActorMessageTag.MSG;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class TestPingPong {
	public TestPingPong() {
		ActorSystem system = new ActorSystem("actor4j::TestPingPong");
		//system.setParallelismFactor(1);
		system.setParallelismMin(1);
		system.softMode();
		
		ActorGroup group = new ActorGroup();
		int size = 100;
		UUID dest = null;
		UUID id = null;
		for(int i=0; i<size; i++) {
			dest = system.addActor(Destination.class);
			id = system.addActor(Client.class, dest);
			group.add(id);
		}
		
		system.broadcast(new ActorMessage<Object>(new Object(), MSG, dest, null), group);
		
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestPingPong();
	}
}
