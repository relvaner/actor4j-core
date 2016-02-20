/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ring.nfold.bulk;

import static actor4j.benchmark.ring.nfold.bulk.ActorMessageTag.RUN;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class TestNFoldRing {
	public TestNFoldRing() {
		ActorSystem system = new ActorSystem("actor4j::TestNFoldRing-Bulk");
		//system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		
		for (int j=0; j<Runtime.getRuntime().availableProcessors(); j++) {
			ActorGroup group = new ActorGroup();
			
			UUID next = system.addActor(Forwarder.class, group);
			group.add(next); // TODO temporary
			int size = 10000;
			for(int i=0; i<size-2; i++) {
				next = system.addActor(Forwarder.class, group, next);
				group.add(next); // TODO temporary
			}
			UUID sender = system.addActor(Sender.class, group, next);
			group.add(sender); // TODO temporary
		
			system.send(new ActorMessage<>(new Object(), RUN, sender, sender));
		}
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestNFoldRing();
	}
}
