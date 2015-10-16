/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4.core.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import actor4j.core.Actor;
import actor4j.core.ActorGroup;
import actor4j.core.messages.ActorMessage;

public class HubPattern {
	protected Actor actor;
	
	protected List<UUID> ports;

	public HubPattern(Actor actor) {
		super();
		
		this.actor = actor;
		
		ports = new LinkedList<>();
	}
	
	public HubPattern(Actor actor, ActorGroup group) {
		this(actor);
		
		ports = group;
	}
	
	public void add(UUID id) {
		ports.add(id);
	}
	
	public void addAll(ActorGroup group) {
		ports.addAll(group);
	}
	
	public void broadcast(ActorMessage<?> message) {
		for (UUID dest : ports)
			actor.send(message, dest);
	}
}
