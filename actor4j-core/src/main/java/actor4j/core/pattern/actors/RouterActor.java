/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorMessageMatcher;

public class RouterActor extends Actor {
	protected ActorMessageMatcher matcher;
	
	public RouterActor(List<RouteeHandler> routees) {
		this(null, routees);
	}
	
	public RouterActor(String name, List<RouteeHandler> routees) {
		super(name);
		
		matcher = new ActorMessageMatcher();
		
		for (RouteeHandler handler : routees)
			add(handler.getPredicate(), handler.getRoutee());
		matcher.matchElse(msg -> unhandled(msg));
	}

	@Override
	public void receive(ActorMessage<?> message) {
		matcher.apply(message);
	}
	
	public void add(Predicate<ActorMessage<?>> predicate, UUID routee) {
		matcher.match(predicate, (msg) -> forward(msg, routee));
	}
}
