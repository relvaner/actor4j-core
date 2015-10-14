/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import static actor4j.core.ActorLogger.logger;
import static actor4j.core.ActorProtocolTag.*;
import static actor4j.core.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import actor4j.function.Consumer;

public class RestartProtocol {
	protected final Actor actor;

	public RestartProtocol(Actor actor) {
		this.actor = actor;
	}
	
	protected void postStop() {
		actor.postStop();
		actor.internal_stop();
		logger().info(String.format("System - actor (%s) stopped", actorLabel(actor)));
	}
	
	protected void postRestart(Exception reason) {
		actor.postStop();
		UUID buf = actor.getId();
		try {
			ActorSystem system = actor.getSystem();
			UUID parent = actor.getParent();
			Actor newActor = (Actor)system.container.getInstance(buf);
			newActor.setId(buf);	
			newActor.parent = parent;
			system.system_addActor(newActor);
			newActor.postRestart(reason);
			logger().info(String.format("System - actor (%s) restarted", actorLabel(actor))); 
		} catch (Exception e) {
			throw new ActorInitializationException(); // never must occur
		}
	}
	
	public void apply(final Exception reason) {
		final List<UUID> waitForChildren =new ArrayList<>(actor.getChildren().size());
		
		Iterator<UUID> iterator = actor.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			actor.watch(dest);
		}
		iterator = actor.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			waitForChildren.add(dest);
			actor.getSystem().sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP, actor.getSelf(), dest));
		}
		
		if (waitForChildren.isEmpty()) 
			postRestart(reason);
		else
			actor.become(new Consumer<ActorMessage<?>>() {
				protected boolean flag_stop;
				@Override
				public void accept(ActorMessage<?> message) {
					if (message.tag==INTERNAL_STOP)
						flag_stop = true;
					else if (message.tag==INTERNAL_STOP_SUCCESS) {
						waitForChildren.remove(message.source);
						if (waitForChildren.isEmpty()) {
							if (flag_stop)
								postStop();
							else {
								postRestart(reason);
								actor.unbecome();
								actor.activeDirectiveBehaviour = false;
							}
						}
					}
				}
			}, false);
	}
}
