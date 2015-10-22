/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class ActorTimer {
	protected ActorSystem system;
	protected UUID id;
	
	protected Timer timer;
	
	protected static int index;
	
	public ActorTimer(ActorSystem system) {
		super();
		
		this.system = system;
		timer = new Timer("actor4j-timer-thread-"+index, false);
		
		id = UUID.randomUUID();
	}
	
	public UUID getId() {
		return id;
	}
	
	public ActorTimer scheduleOnce(final ActorMessage<?> message, final UUID dest, long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				message.dest = dest;
				system.send(message);
			}
		}, delay); 
		
		return this;
	}
	
	public ActorTimer scheduleOnce(final ActorMessage<?> message, final ActorGroup group, long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (UUID id : group) {
					message.dest = id;
					system.send(message);
				}
			}
		}, delay); 
		
		return this;
	}
	
	public ActorTimer schedule(final ActorMessage<?> message, final UUID dest, long delay, long period) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				message.dest = dest;
				system.send(message);
			}
		}, delay, period); 
		
		return this;
	}
	
	public ActorTimer schedule(final ActorMessage<?> message, final ActorGroup group, long delay, long period) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				for (UUID id : group) {
					message.dest = id;
					system.send(message);
				}
			}
		}, delay, period); 
		
		return this;
	}
	
	public void cancel() {
		timer.cancel();
		system.executerService.actorTimers.remove(timer);
	}
}
