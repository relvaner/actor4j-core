/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.utils;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class HubPattern {
	protected Actor actor;
	
	protected ActorGroup ports;

	public HubPattern(Actor actor) {
		super();
		
		this.actor = actor;
		
		ports = new ActorGroup();
	}
	
	public HubPattern(Actor actor, ActorGroup group) {
		this(actor);
		
		ports = group;
	}
	
	public ActorGroup getPorts() {
		return ports;
	}

	public void add(UUID id) {
		ports.add(id);
	}
	
	public void addAll(ActorGroup group) {
		ports.addAll(group);
	}
	
	public void remove(UUID id) {
		ports.remove(id);
	}
	
	public boolean contains(UUID id) {
		return ports.contains(id);
	}
	
	public int count() {
		return ports.size();
	}
	
	public void broadcast(ActorMessage<?> message) {
		for (UUID dest : ports)
			actor.send(message, dest);
	}
}
