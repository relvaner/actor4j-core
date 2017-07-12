/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.reactive.streams;

import java.util.UUID;
import java.util.function.Consumer;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import tools4j.function.Procedure;

public class ProcessorActor extends Actor {
	protected ProcessorImpl processorImpl;
	
	public ProcessorActor() {
		this(null);
	}
	
	public ProcessorActor(String name) {
		super(name);
		processorImpl = new ProcessorImpl(this);
	}
	
	@Override
	public void receive(ActorMessage<?> message) {
		processorImpl.receive(message);
	}
	
	public <T> void broadcast(T value) {
		processorImpl.broadcast(value);
	}
	
	public boolean isBulk(UUID dest) {
		return processorImpl.isBulk(dest);
	}
	
	public <T> boolean onNext(T value, UUID dest) {
		return processorImpl.onNext(value, dest);
	}
	
	public void onError(String error, UUID dest) {
		processorImpl.onError(error, dest);
	}
	
	public void onComplete(UUID dest) {
		processorImpl.onComplete(dest);
	}
	
	public void subscribe(UUID dest, Consumer<Object> onNext, Consumer<String> onError, Procedure onComplete) {
		processorImpl.subscribe(dest, onNext, onError, onComplete);
	}
	
	public void unsubscribe(UUID dest) {
		processorImpl.unsubscribe(dest);
	}
	
	public void request(long n, UUID dest) {
		processorImpl.request(n, dest);
	}
	
	public void requestReset(long n, UUID dest) {
		processorImpl.requestReset(n, dest);
	}
	
	public void bulk(UUID dest) {
		processorImpl.bulk(dest);
	}
	
	public void cancelBulk(UUID dest) {
		processorImpl.cancelBulk(dest);
	}
}
