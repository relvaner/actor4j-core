package actor4j.benchmark.akka.hub;

import java.util.concurrent.atomic.AtomicLong;

import actor4j.benchmark.akka.ActorMessage;
import actor4j.benchmark.akka.Benchmark;
import actor4j.function.Supplier;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import static actor4j.benchmark.akka.hub.ActorMessageTag.*;

public class TestHub {
	public TestHub() {
		ActorSystem system = ActorSystem.create("test");
		
		final AtomicLong counter = new AtomicLong();
		ActorRef dest = system.actorOf(Props.create(Destination.class, counter).withDispatcher("my-dispatcher"));
		
		HubPattern hub = new HubPattern();
		int size = 1000;
		ActorRef ref = null;
		for(int i=0; i<size; i++) {
			ref = system.actorOf(Props.create(Client.class, dest).withDispatcher("my-dispatcher"));
			hub.add(ref);
		}
		hub.broadcast(new ActorMessage(RUN), dest);
		
		Benchmark benchmark = new Benchmark(system, new Supplier<Long>() {
			@Override
			public Long get() {
				return counter.get();
			}
		}, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestHub();
	}
}
