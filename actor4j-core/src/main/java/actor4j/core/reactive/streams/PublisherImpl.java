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
	protected Map<UUID, Boolean> bulks;
	
	public PublisherImpl(Actor actor) {
		super();
		this.actor = actor;
		subscribers = new ActorGroup();
		requests = new HashMap<>();
		bulks = new HashMap<>();
	}
	
	public void receive(ActorMessage<?> message) {
		if (message.source!=null) {
			if (message.tag==SUBSCRIPTION_REQUEST || message.tag==SUBSCRIPTION_REQUEST_RESET) { //Validation: Long -> OnError
				long request = message.valueAsLong();
				if (!subscribers.add(message.source) && message.tag==SUBSCRIPTION_REQUEST) {
					request += requests.get(message.source);
					if (Long.MAX_VALUE-request<0)
						request = Long.MAX_VALUE;
				}
			
				requests.put(message.source, request);
			}
			else if (message.tag==SUBSCRIPTION_CANCEL)
				cancel(message.source);
			else if (message.tag==SUBSCRIPTION_BULK)
				bulks.put(message.source, true);
			else if (message.tag==SUBSCRIPTION_CANCEL_BULK)
				bulks.remove(message.source);
		}
	}
	
	public void cancel(UUID dest) {
		subscribers.remove(dest);
		requests.remove(dest);
	}
	
	public <T> void broadcast(T value) {
		for (UUID dest: subscribers)
			onNext(value, dest);
	}
	
	public boolean isBulk(UUID dest) {
		return bulks.get(dest)!=null;
	}
	
	public <T> boolean onNext(T value, UUID dest) {
		boolean result = false;
		
		if (dest!=null) {
			Long request = requests.get(dest);
			
			if (request!=null) {
				if (request==Long.MAX_VALUE)
					actor.tell(value, ON_NEXT, dest);
				else if (request>0) {
					requests.put(dest, request-1);
					actor.tell(value, ON_NEXT, dest);
				
					if (request==1)
						onComplete(dest);
				}
				
				result = true;
			}
		}
		
		return result;
	}
	
	public void onError(String error, UUID dest) {
		actor.tell(error, ON_ERROR, dest);
		cancel(dest);
	}
	
	public void onComplete(UUID dest) {
		actor.tell(null, ON_COMPLETE, dest);
		cancel(dest);
	}
}
