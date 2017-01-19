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
			if (message.value instanceof Publish) {
				String buf = ((Publish<?>)message.value).topic;
				if (topic.equals(buf))
					hub.broadcast(message);
			}
			else if (message.value instanceof Subscribe) {
				String buf = ((Subscribe)message.value).topic;
				if (topic.equals(buf))
					hub.add(message.source);
			}
			else
				unhandled(message);
		}
		else
			unhandled(message);
	}
}
