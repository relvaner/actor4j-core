package actor4j.benchmark.akka.ring;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import actor4j.benchmark.akka.Benchmark;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class TestRing {
	public TestRing() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ActorSystem system = ActorSystem.create("akka-benchmark-ring");
		
		final AtomicLong counter = new AtomicLong();
		int size = 100;
		ActorRef next = system.actorOf(Props.create(Forwarder.class).withDispatcher("my-dispatcher"));
		for(int i=0; i<size-2; i++) {
			next = system.actorOf(Props.create(Forwarder.class, next).withDispatcher("my-dispatcher"));
		}
		ActorRef sender = system.actorOf(Props.create(Sender.class, counter, next, size).withDispatcher("my-dispatcher"));
		sender.tell(new Object(), sender);
		
		Benchmark benchmark = new Benchmark(system, new Supplier<Long>() {
			@Override
			public Long get() {
				return counter.get();
			}
		}, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestRing();
	}
}
