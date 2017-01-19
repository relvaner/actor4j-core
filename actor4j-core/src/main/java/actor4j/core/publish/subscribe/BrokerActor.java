/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.publish.subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class BrokerActor extends Actor {
	protected Map<String, UUID> topics;
	
	public static final int GET_TOPIC_ACTOR = 100;
	
	public BrokerActor() {
		this("broker-actor");
	}
	
	public BrokerActor(String name) {
		super(name);
		
		topics = new HashMap<>();
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null) {
			if (message.value instanceof Publish || message.value instanceof Subscribe) {
				final String topic;
				if (message.value instanceof Publish)
					topic = ((Publish<?>)message.value).topic;
				else
					topic = ((Subscribe)message.value).topic;
				
				UUID dest = topics.get(topic);
				if (dest==null) {
					dest = addChild(() -> new TopicActor("topic-actor:"+topic, topic));
					topics.put(topic, dest);
				}
				if (message.tag==GET_TOPIC_ACTOR)
					tell(dest, message.tag, message.source);
				forward(message, dest);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
