/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ring;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;

public class TestRing {
	public TestRing() {
		ActorSystem system = new ActorSystem();
		//system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.hardMode();
		
		int size = 100;
		UUID next = system.addActor(Forwarder.class);
		for(int i=0; i<size-2; i++) {
			next = system.addActor(Forwarder.class, next);
		}
		UUID sender = system.addActor(Sender.class, next);
		
		system.send(new ActorMessage<>(new Object(), 0, sender, sender));
		
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestRing();
	}
}
