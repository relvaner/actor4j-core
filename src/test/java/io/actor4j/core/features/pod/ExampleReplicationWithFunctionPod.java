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
package io.actor4j.core.features.pod;

import static io.actor4j.core.logging.ActorLogger.*;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.functions.FunctionPod;
import io.actor4j.core.pods.functions.PodFunction;
import io.actor4j.core.utils.Pair;

public class ExampleReplicationWithFunctionPod extends FunctionPod {
	@Override
	public PodFunction createFunction(ActorRef host, PodContext context) {
			return new PodFunction(host, context) {
				@Override
				public Pair<Object, Integer> handle(ActorMessage<?> message) {
					logger().log(DEBUG, message.value.toString());
					
					/*
					host.tell(String.format("Hello %s! [domain:%s, primaryReplica:%s]", 
							message.value, 
							context.getDomain(),
							context.isPrimaryReplica())
							, 42, message.source, message.interaction);
					return null;
					*/
					
					return Pair.of(String.format("Hello %s! [domain:%s, primaryReplica:%s]", 
							message.value, 
							context.getDomain(),
							context.isPrimaryReplica()), 42);
				}
			};
	}

	@Override
	public String domain() {
		return "ExampleReplicationWithFunctionPod";
	}
}
