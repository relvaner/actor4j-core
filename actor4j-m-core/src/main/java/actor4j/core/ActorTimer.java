/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;
import actor4j.function.Supplier;

public class ActorTimer {
	protected ActorSystemImpl system;
	protected UUID id;
	
	protected Timer timer;
	
	protected static final AtomicInteger index;
	
	static {
		index = new AtomicInteger(-1);
	}
	
	public ActorTimer(ActorSystemImpl system) {
		super();
		
		this.system = system;
		timer = new Timer("actor4j-timer-thread-"+index.getAndIncrement(), false);
		
		id = UUID.randomUUID();
	}
	
	public UUID getId() {
		return id;
	}
	
	public ActorTimer scheduleOnce(final Supplier<ActorMessage<?>> supplier, final UUID dest, long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				message.dest = dest;
				system.send(message);
			}
		}, delay); 
		
		return this;
	}
	
	public ActorTimer scheduleOnce(final ActorMessage<?> message, final UUID dest, long delay) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, dest, delay);
	}
	
	public ActorTimer scheduleOnce(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				for (UUID id : group) {
					message.dest = id;
					system.send(message);
				}
			}
		}, delay); 
		
		return this;
	}
	
	public ActorTimer scheduleOnce(final ActorMessage<?> message, final ActorGroup group, long delay) {
		return scheduleOnce(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, group, delay);
	}
	
	public ActorTimer schedule(final Supplier<ActorMessage<?>> supplier, final UUID dest, long delay, long period) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				message.dest = dest;
				system.send(message);
			}
		}, delay, period); 
		
		return this;
	}
	
	public ActorTimer schedule(final ActorMessage<?> message, final UUID dest, long delay, long period) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, dest, delay, period);
	}
	
	public ActorTimer schedule(final Supplier<ActorMessage<?>> supplier, final ActorGroup group, long delay, long period) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ActorMessage<?> message = supplier.get();
				for (UUID id : group) {
					message.dest = id;
					system.send(message);
				}
			}
		}, delay, period); 
		
		return this;
	}
	
	public ActorTimer schedule(final ActorMessage<?> message, final ActorGroup group, long delay, long period) {
		return schedule(new Supplier<ActorMessage<?>>() {
			@Override
			public ActorMessage<?> get() {
				return message;
			}
		}, group, delay, period);
	}
	
	public void cancel() {
		timer.cancel();
		system.getExecuterService().actorTimers.remove(this);
	}
}
