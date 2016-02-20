/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.bcast;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;
import actor4j.research.design.flow.FlowActorSystemImpl;

public class TestBcast {
	public TestBcast() {
		//ActorSystem system = new ActorSystem("actor4j::Bcast");
		ActorSystem system = new ActorSystem("actor4j::Bcast", FlowActorSystemImpl.class);
		//system.setParallelismFactor(1);
		//system.setParallelismMin(1);
		system.hardMode();
		
		ActorGroup group = new ActorGroup();
		int size = 100;
		UUID id = null;
		for(int i=0; i<size; i++) {
			id = system.addActor(TestActor.class, group);
			group.add(id);
		}
		
		system.broadcast((new ActorMessage<Object>(new Object(), 0, id, null)), group);
		
		
		Benchmark benchmark = new Benchmark(system, 30000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestBcast();
	}
}
