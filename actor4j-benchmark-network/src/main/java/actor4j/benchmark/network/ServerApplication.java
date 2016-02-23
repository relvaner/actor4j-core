/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.network;

import java.text.DecimalFormat;
import java.util.UUID;

import javax.ws.rs.ApplicationPath;

import actor4j.core.ActorService;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import actor4j.core.ActorTimer;
import actor4j.server.RESTActorApplication;

@ApplicationPath("api")
public class ServerApplication extends RESTActorApplication {
	@Override
	protected void configure(ActorService service) {
		service.setParallelismMin(1);
		service.setParallelismFactor(1);
		service.hardMode();
		
		UUID server = service.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new Server();
			}
		});
		service.setAlias(server, "server");
		System.out.println(server);
		
		service.addActor(() -> new Actor("benchmark") {
			protected long iteration;
			protected long lastCount;
			protected DecimalFormat decimalFormat;
			protected ActorTimer timer;
			
			@Override
			public void preStart() {
				iteration = 1;
				decimalFormat = new DecimalFormat("###,###,###");
				
				timer = service.timer().schedule(new ActorMessage<>(null, 0, service.SYSTEM_ID, null), self(), 1000, 1000);
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				if (iteration>=120) {
					timer.cancel();
					tell(null, Actor.POISONPILL, server);
				}
					
				long count = service.underlyingImpl().getExecuterService().getCount();
				long diff  = count-lastCount;
				
				System.out.printf("%-2d : %s msg/s%n", ++iteration, decimalFormat.format(diff));
				
				lastCount = count;
			}
		});
	}
}
