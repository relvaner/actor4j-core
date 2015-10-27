/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.protocols;

import static actor4j.core.protocols.ActorProtocolTag.*;
import static actor4j.core.utils.ActorLogger.logger;
import static actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import actor4j.core.ActorCell;
import actor4j.core.actors.Actor;
import actor4j.core.exceptions.ActorInitializationException;
import actor4j.core.messages.ActorMessage;
import actor4j.function.Consumer;

public class RestartProtocol {
	protected final ActorCell cell;

	public RestartProtocol(ActorCell cell) {
		this.cell = cell;
	}
	
	protected void postStop() {
		cell.postStop();
		cell.internal_stop();
		logger().info(String.format("%s - System: actor (%s) stopped", cell.getSystem().getName(), actorLabel(cell.getActor())));
	}
	
	protected void postRestart(Exception reason) {
		cell.postStop();
		try {
			Actor newActor = (Actor)cell.getSystem().getContainer().getInstance(cell.getId());
			newActor.setCell(cell);
			cell.setActor(newActor);
			cell.postRestart(reason);
			logger().info(String.format("%s - System: actor (%s) restarted", cell.getSystem().getName(), actorLabel(cell.getActor()))); 
		} catch (Exception e) {
			throw new ActorInitializationException(); // never must occur
		}
	}
	
	public void apply(final Exception reason) {
		final List<UUID> waitForChildren =new ArrayList<>(cell.getChildren().size());
		
		Iterator<UUID> iterator = cell.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			cell.watch(dest);
		}
		iterator = cell.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			waitForChildren.add(dest);
			cell.getSystem().sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP, cell.getId(), dest));
		}
		
		if (waitForChildren.isEmpty()) 
			postRestart(reason);
		else
			cell.become(new Consumer<ActorMessage<?>>() {
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
								cell.unbecome();
								cell.setActiveDirectiveBehaviour(false);
							}
						}
					}
				}
			}, false);
	}
}
