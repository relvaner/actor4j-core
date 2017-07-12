package akka.benchmark.ring.nfold;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.benchmark.Benchmark;

public class TestNFoldRing {
	public TestNFoldRing() {	
		ActorSystem system = ActorSystem.create("akka-benchmark-nfold-ring");
		
		final AtomicLong counter = new AtomicLong();
		
		for(int j=0; j<Runtime.getRuntime().availableProcessors(); j++) {
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
		new TestNFoldRing();
	}
}
