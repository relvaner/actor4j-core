package actor4j.benchmark.ejb;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class Ping {
	@EJB
	Pong pong;
	
	@Asynchronous
	public void receive(ActorMessage message) {
		Benchmark.counter.incrementAndGet();
		pong.receive(new ActorMessage(new Object(), 0));
	}
}
