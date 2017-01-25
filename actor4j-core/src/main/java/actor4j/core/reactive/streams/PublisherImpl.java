/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.reactive.streams;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

import static actor4j.core.reactive.streams.ReactiveStreamsTag.*;

public class PublisherImpl {
	protected Actor actor;
	
	protected ActorGroup subscribers;
	protected Map<UUID, Long> requests;
	
	public PublisherImpl(Actor actor) {
		super();
		this.actor = actor;
		subscribers = new ActorGroup();
		requests = new HashMap<>();
	}
	
	public void receive(ActorMessage<?> message) {
		if (message.source!=null) {
			if (message.tag==SUBSCRIPTION_REQUEST || message.tag==SUBSCRIPTION_RESET_REQUEST) { //Validierung: Integer -> OnError
				long request = 0;
				if (!subscribers.add(message.source) && message.tag==SUBSCRIPTION_REQUEST)
					request = requests.get(message.source);
			
				requests.put(message.source, request+message.valueAsLong());
			}
			else if (message.tag==SUBSCRIPTION_CANCEL) {
				subscribers.remove(message.source);
				requests.remove(message.source);
			}
		}
	}
	
	public <T> void broadcast(T value) {
		for (UUID dest: subscribers)
			signalOnNext(value, dest);
	}
	
	public <T> boolean signalOnNext(T value, UUID dest) {
		boolean result = false;
		
		if (dest!=null) {
			Long request = requests.get(dest);
			
			if (request!=null && request>0) {
				requests.put(dest, request-1);
				actor.tell(value, ON_NEXT, dest);
				
				if (request==1)
					signalOnComplete(dest);
				
				result = true;
			}
		}
		
		return result;
	}
	
	public void signalOnError(String error, UUID dest) {
		actor.tell(error, ON_ERROR, dest);
	}
	
	public void signalOnComplete(UUID dest) {
		actor.tell(null, ON_COMPLETE, dest);
	}
}
