package actor4j.benchmark.akka.bcast;

import java.util.function.Supplier;

import actor4j.benchmark.akka.ActorMessage;
import actor4j.benchmark.akka.Benchmark;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class TestBcast {
	public TestBcast() {
		ActorSystem system = ActorSystem.create("akka-benchmark-bcast");
		
		final HubPattern hub = new HubPattern();
		int size = 2;
		ActorRef ref = null;
		for(int i=0; i<size; i++) {
			ref = system.actorOf(Props.create(TestActor.class, hub).withDispatcher("my-dispatcher"));
			hub.add(ref);
		}
		hub.broadcast(new ActorMessage(0), ref);
		
		Benchmark benchmark = new Benchmark(system, new Supplier<Long>() {
			@Override
			public Long get() {
				return hub.getCount();
			}
		}, 30000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestBcast();
	}
}
