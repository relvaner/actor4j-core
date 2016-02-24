/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.prisma;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class TestPrisma {
	public TestPrisma() {
		ActorSystem system = new ActorSystem();
		//system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.hardMode();
		
		for (int j=0; j<2; j++) {
			ActorGroup group1 = new ActorGroup();
			ActorGroup group2 = new ActorGroup();
			
			UUID next1 = system.addActor(Forwarder.class, group1);
			UUID next2 = system.addActor(Forwarder.class, group2);
			int size = 100;
			for(int i=0; i<size; i++) {
				next1 = system.addActor(Forwarder.class, group1, next1);
				next2 = system.addActor(Forwarder.class, group2, next2, next1);
				group2.add(next2); // TODO temporary
			}
			system.broadcast(new ActorMessage<Object>(new Object(), 0, system.SYSTEM_ID, null), group2);
		}
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestPrisma();
	}
}
