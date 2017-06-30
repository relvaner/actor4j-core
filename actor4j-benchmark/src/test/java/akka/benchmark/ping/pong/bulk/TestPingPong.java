package akka.benchmark.ping.pong.bulk;

import static akka.benchmark.ping.pong.bulk.ActorMessageTag.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.benchmark.ActorMessage;
import akka.benchmark.Benchmark;

public class TestPingPong {
	public TestPingPong() {
		ActorSystem system = ActorSystem.create("akka-benchmark-ping-pong-bulk");
		
		final AtomicLong counter = new AtomicLong();
		
		HubPattern hub = new HubPattern();
		int size = 10;
		ActorRef dest = null;
		ActorRef ref = null;
		for(int i=0; i<size; i++) {
			dest = system.actorOf(Props.create(Destination.class, counter).withDispatcher("my-dispatcher"));
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
		new TestPingPong();
	}
}
