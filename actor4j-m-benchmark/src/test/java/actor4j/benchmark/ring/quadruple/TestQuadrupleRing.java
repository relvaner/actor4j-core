/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ring.quadruple;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorGroup;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;

public class TestQuadrupleRing {
	public TestQuadrupleRing() {
		ActorSystem system = new ActorSystem();
		//system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		
		for (int j=0; j<4; j++) {
			ActorGroup group = new ActorGroup();
			
			UUID next = system.addActor(new Forwarder(group, null));
			int size = 100;
			for(int i=0; i<size-2; i++) {
				next = system.addActor(new Forwarder(group, next));
			}
			UUID sender = system.addActor(new Sender(group, next));
		
			system.send(new ActorMessage<>(new Object(), 0, sender, sender));
		}
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestQuadrupleRing();
	}
}
