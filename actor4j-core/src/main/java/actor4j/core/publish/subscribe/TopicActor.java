/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.publish.subscribe;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.HubPattern;

public class TopicActor extends Actor {
	protected String topic;
	protected HubPattern hub; 
	
	public TopicActor(String name, String topic) {
		super(name);
		this.topic = topic;
		
		hub = new HubPattern(this);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		if (message.value!=null) {
			String buf = ((Topic)message.value).topic;
			if (topic.equals(buf)) {
				if (message.value instanceof Publish)
					hub.broadcast(message);
				else if (message.value instanceof Subscribe && message.tag==BrokerActor.INTERNAL_FORWARDED_BY_BROKER)
					hub.add(message.source);
				else if (message.value instanceof Unsubscribe && message.tag==BrokerActor.INTERNAL_FORWARDED_BY_BROKER) {
					hub.remove(message.source);
					if (hub.count()==0)
						stop();
				}
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
