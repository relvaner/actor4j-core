/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import static actor4j.core.ActorProtocolTag.*;
import static actor4j.core.utils.ActorLogger.logger;
import static actor4j.core.utils.ActorUtils.actorLabel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import actor4j.core.messages.ActorMessage;
import actor4j.function.Consumer;

public class StopProtocol {
	protected final ActorCell cell;

	public StopProtocol(ActorCell cell) {
		this.cell = cell;
	}
	
	protected void postStop() {
		cell.postStop();
		cell.internal_stop();
		logger().info(String.format("%s - System: actor (%s) stopped", cell.system.name, actorLabel(cell.actor)));
	}
	
	public void apply() {
		final List<UUID> waitForChildren =new ArrayList<>(cell.children.size());
		
		Iterator<UUID> iterator = cell.children.iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			cell.watch(dest);
		}
		iterator = cell.children.iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			waitForChildren.add(dest);
			cell.watch(dest);
			cell.system.sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP, cell.id, dest));
		}
		
		if (waitForChildren.isEmpty()) 
			postStop();
		else
			cell.become(new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					if (message.tag==INTERNAL_STOP_SUCCESS) {
						waitForChildren.remove(message.source);
						if (waitForChildren.isEmpty())
							postStop();
					}
				}
			});
	}
}
