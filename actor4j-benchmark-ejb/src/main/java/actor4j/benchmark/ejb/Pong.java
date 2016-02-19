package actor4j.benchmark.ejb;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class Pong {
	@EJB
	Ping ping;
	
	@Asynchronous
	public void receive(ActorMessage message) {
		Benchmark.counter.incrementAndGet();
		ping.receive(new ActorMessage(new Object(), 0));
	}
}
