package actor4j.benchmark.bcast;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorGroup;
import actor4j.core.ActorMessage;
import actor4j.core.ActorSystem;
import actor4j.core.HubPattern;

public class TestBcast {
	public TestBcast() {
		ActorSystem system = new ActorSystem();
		system.setParallelismFactor(1);
		system.setParallelismMin(1);
		system.softMode();
		
		
		ActorGroup group = new ActorGroup();
		HubPattern hub = new HubPattern(system);
		int size = 100;
		UUID id = null;
		for(int i=0; i<size; i++) {
			id = system.addActor(new TestActor(hub));
			group.add(id);
		}
		hub.addAll(group);
		
		system.broadcast((new ActorMessage<Object>(new Object(), 0, id, null)), group);
		
		
		Benchmark benchmark = new Benchmark(system, 30000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestBcast();
	}
}
