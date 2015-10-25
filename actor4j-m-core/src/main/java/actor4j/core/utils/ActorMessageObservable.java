package actor4j.core.utils;

import java.util.Queue;

import actor4j.core.messages.ActorMessage;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Producer;
import rx.Subscriber;

public class ActorMessageObservable {
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
