/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.publish.subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

/**
 * publish over broker or directly to the topic actor (Tag=GET_TOPIC_ACTOR), watch(TOPIC_ACTOR) on starvation (no subscribers)
 * subscribe && unsubscribe only over the broker
 */
public class BrokerActor extends Actor {
	protected Map<String, UUID> topics;
	protected Map<String, Integer> counter;
	
	public static final int GET_TOPIC_ACTOR = 100;
	protected static final int FORWARDED_BY_BROKER = 101;
	
	public BrokerActor() {
		this("broker-actor");
	}
	
	public BrokerActor(String name) {
		super(name);
		
		topics = new HashMap<>();
		counter = new HashMap<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null) {
			if (message.value instanceof Topic) {
				final String topic = ((Topic)message.value).topic;
				UUID dest = topics.get(topic);
				if (dest==null) {
					if (message.value instanceof Unsubscribe)
						return; // Abort, the topic was not found
					dest = addChild(() -> new TopicActor("topic-actor:"+topic, topic));
					topics.put(topic, dest);
					counter.put(topic, 0);
				}
				if (message.value instanceof Publish) {
					if (message.tag==GET_TOPIC_ACTOR)
						tell(dest, message.tag, message.source);
				}
				else if (message.value instanceof Subscribe) {
					message.tag = FORWARDED_BY_BROKER;
					counter.put(topic, counter.get(topic)+1);
				}
				else if (message.value instanceof Unsubscribe) {
					message.tag = FORWARDED_BY_BROKER;
					if (counter.get(topic)-1==0) {
						topics.remove(topic);
						counter.remove(topic);
					}
				}
				forward(message, dest);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
