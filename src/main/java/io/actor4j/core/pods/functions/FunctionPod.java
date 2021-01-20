/*
 * Copyright (c) 2015-2021, David A. Bauer. All rights reserved.
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
package io.actor4j.core.pods.functions;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.ActorPod;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.actors.PodActor;
import io.actor4j.core.utils.Pair;

public abstract class FunctionPod extends ActorPod {
	@Override
	public PodActor create() {
		return new PodActor() {
			protected PodFunction podFunction;
			
			@Override
			public void preStart() {
				if (getContext().isShard())
					setAlias(domain()+getContext().getShardId());
				else
					setAlias(domain());
				
				 register();
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				Pair<Object, Integer> result = podFunction.handle(message);
				if (result!=null)
					internal_callback(this, message, result);
			}

			@Override
			public void register() {
				podFunction = createFunction(this, getContext());
			}
		};
	}
	
	protected void internal_callback(ActorRef host, ActorMessage<?> message, Pair<Object, Integer> result) {
		host.tell(result.a, result.b, message.source, message.interaction, message.protocol, message.domain);
	}

	public abstract PodFunction createFunction(ActorRef host, PodContext context);
}
