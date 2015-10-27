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
		logger().info(String.format("%s - System: actor (%s) stopped", cell.getSystem().getName(), actorLabel(cell.getActor())));
	}
	
	public void apply() {
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
			cell.watch(dest);
			cell.getSystem().sendAsDirective(new ActorMessage<>(null, INTERNAL_STOP, cell.getId(), dest));
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
