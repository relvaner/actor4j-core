package actor4j.benchmark.akka.ring.quadruple;

import java.util.concurrent.atomic.AtomicLong;

import actor4j.benchmark.akka.Benchmark;
import actor4j.function.Supplier;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class TestQuadrupleRing {
	public TestQuadrupleRing() {	
		ActorSystem system = ActorSystem.create("akka-benchmark-quadruple-ring");
		
		final AtomicLong counter = new AtomicLong();
		
		for(int j=0; j<4; j++) {
			int size = 100;
			ActorRef next = system.actorOf(Props.create(Forwarder.class).withDispatcher("my-dispatcher"));
			for(int i=0; i<size-2; i++) {
				next = system.actorOf(Props.create(Forwarder.class, next).withDispatcher("my-dispatcher"));
			}
			ActorRef sender = system.actorOf(Props.create(Sender.class, counter, next, size).withDispatcher("my-dispatcher"));
			sender.tell(new Object(), sender);
		}
		
		Benchmark benchmark = new Benchmark(system, new Supplier<Long>() {
			@Override
			public Long get() {
				return counter.get();
			}
		}, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestQuadrupleRing();
	}
}
