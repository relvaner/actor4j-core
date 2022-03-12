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

import java.util.UUID;

import io.actor4j.core.actors.ActorRef;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.pods.PodContext;
import io.actor4j.core.pods.RemotePodMessage;
import io.actor4j.core.pods.functions.PodRemoteFunction;
import io.actor4j.core.pods.functions.RemoteFunctionPod;
import io.actor4j.core.utils.Pair;

public class ExampleReplicationWithRemoteFunctionPod extends RemoteFunctionPod {
	@Override
	public PodRemoteFunction createFunction(ActorRef host, PodContext context) {
			ExampleReplicationWithRemoteFunctionImpl functionImpl = new ExampleReplicationWithRemoteFunctionImpl(host, context);
		
			return new PodRemoteFunction(host, context) {
				@Override
				public Pair<Object, Integer> handle(ActorMessage<?> message) {
					return functionImpl.handle(message);
				}

				@Override
				public Pair<Object, Integer> handle(RemotePodMessage remoteMessage, UUID interaction) {
					return functionImpl.handle(ActorMessage.create(remoteMessage.remotePodMessageDTO().payload(), remoteMessage.remotePodMessageDTO().tag(), host.self(), null, interaction, "", ""));
				}
			};
	}

	@Override
	public String domain() {
		return "ExampleReplicationWithRemoteFunctionPod";
	}
}
