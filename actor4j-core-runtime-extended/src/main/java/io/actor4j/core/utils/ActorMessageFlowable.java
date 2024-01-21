/*
 * Copyright (c) 2015-2024, David A. Bauer. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.utils;

import java.util.Queue;

import io.actor4j.core.messages.ActorMessage;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;

public class ActorMessageFlowable {
	public static Flowable<ActorMessage<?>> getMessages(final Queue<ActorMessage<?>> stash) {
		return Flowable.create(new FlowableOnSubscribe<ActorMessage<?>>() {
			@Override
			public void subscribe(FlowableEmitter<ActorMessage<?>> emitter) throws Exception {
				try {
					ActorMessage<?> message;
					for (int i=0; !emitter.isCancelled() && i<emitter.requested() && (message=stash.poll())!=null; i++) 
						emitter.onNext(message);
					
					if (emitter.isCancelled())
						return;
					else
						emitter.onComplete();;
				}
				catch (Exception e) {
					emitter.onError(e);
				}
			}
			
		}, BackpressureStrategy.BUFFER);
	}
}
