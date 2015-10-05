package actor4j.server.example;

import java.util.UUID;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorSystem;
import actor4j.server.RESTActorApplication;

@ApplicationPath("rest")
public class ReceiverApplication extends RESTActorApplication {
	@Override
	protected void configure(ActorSystem system) {
		system.setParallelismMin(1);
		system.setParallelismFactor(1);
		system.softMode();
		
		UUID receiver = system.addActor(new Receiver());
		system.setAlias(receiver, "receiver");
		System.out.println(receiver);
	}
}
