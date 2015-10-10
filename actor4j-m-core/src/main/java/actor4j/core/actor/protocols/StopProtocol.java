package actor4j.core.actor.protocols;

import static actor4j.core.actor.protocols.ActorProtocolTag.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorMessage;
import actor4j.function.Consumer;

public class StopProtocol {
	protected final Actor actor;

	public StopProtocol(Actor actor) {
		this.actor = actor;
	}
	
	public void apply(final UUID client, final boolean complete) {
		final List<UUID> waitForChildren =new ArrayList<>(actor.getChildren().size());
		
		Iterator<UUID> iterator = actor.getChildren().iterator();
		while (iterator.hasNext()) {
			UUID dest = iterator.next();
			waitForChildren.add(dest);
			actor.send(new ActorMessage<>(null, INTERNAL_STOP, actor.getSelf(), dest));
		}
		
		if (waitForChildren.isEmpty()) {
			actor.postStop();
			if (complete) actor.internal_stop();
			if (!client.equals(actor.getSelf()))
				actor.send(new ActorMessage<>(null, INTERNAL_STOP_SUCCESS, actor.getSelf(), client));
		}
		else
			actor.become(new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					if (message.tag==INTERNAL_STOP_SUCCESS) {
						waitForChildren.remove(message.source);
						if (waitForChildren.isEmpty()) {
							actor.postStop();
							if (complete) actor.internal_stop();
							if (!client.equals(actor.getSelf()))
								actor.send(new ActorMessage<>(null, INTERNAL_STOP_SUCCESS, actor.getSelf(), client));
						}
					}
					actor.unhandled(message);
				}
			});
	}
}
