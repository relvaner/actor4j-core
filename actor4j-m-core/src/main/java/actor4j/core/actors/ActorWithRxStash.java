/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.actors;

import java.util.LinkedList;
import java.util.Queue;

import actor4j.core.Actor;
import actor4j.core.messages.ActorMessage;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Producer;
import rx.Subscriber;

public abstract class ActorWithRxStash extends Actor {
	protected static class ActorMessageObservable {
		public static Observable<ActorMessage<?>> getMessages(final Queue<ActorMessage<?>> stash) {
			return Observable.create(new OnSubscribe<ActorMessage<?>>() {
				@Override
				public void call(final Subscriber<? super ActorMessage<?>> subscriber) {
					subscriber.setProducer(new Producer() {
						@Override
						public void request(long count) {
							try {
								ActorMessage<?> message;
								for (int i=0; i<count && (message=stash.poll())!=null; i++) 
									subscriber.onNext(message);
								subscriber.onCompleted();
							}
							catch (Exception e) {
								subscriber.onError(e);
							}
						}
					});
				}
			});
		}
	}
	
	protected Observable<ActorMessage<?>> rxStash;
	
	public ActorWithRxStash() {
		this(null);
	}
	
	public ActorWithRxStash(String name) {
		super(name);
		
		stash   = new LinkedList<>();
		rxStash = ActorMessageObservable.getMessages(stash);
	}
	
	public ActorMessage<?> unstash() {
		return stash.poll();
	}
}
