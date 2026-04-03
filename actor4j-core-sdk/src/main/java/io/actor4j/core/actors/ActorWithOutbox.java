/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.actors;

import java.util.LinkedList;
import java.util.Queue;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;

public abstract class ActorWithOutbox extends Actor {
	static record OutboxMessage(ActorMessage<?> message, ActorId dest, String alias) {	
	};
	
	protected final Queue<OutboxMessage> outbox;
	protected boolean outboxEnabled;

	public ActorWithOutbox() {
		this(null);
	}
	
	public ActorWithOutbox(String name) {
		super(name);
		
		outbox = new LinkedList<>();
	}
	
	public void flushOutbox() {
		OutboxMessage outboxMessage = null;
		while ((outboxMessage=outbox.poll())!=null) {
			if (outboxMessage.dest()!=null)
				send(outboxMessage.message(), outboxMessage.dest());
			else if (outboxMessage.alias()!=null)
				sendViaAlias(outboxMessage.message(),  outboxMessage.alias());
			else
				send(outboxMessage.message());
		}
	}

	@Override
	public void send(ActorMessage<?> message) {
		if (outboxEnabled)
			outbox.offer(new OutboxMessage(message, null, null));
		else
			super.send(message);
	}
	
	@Override
	public void send(ActorMessage<?> message, ActorId dest) {
		if (outboxEnabled)
			outbox.offer(new OutboxMessage(message, dest, null));
		else 
			super.send(message, dest);
	}
	
	@Override
	public void sendViaAlias(ActorMessage<?> message, String alias) {
		if (outboxEnabled)
			outbox.offer(new OutboxMessage(message, null, alias));
		else
			super.sendViaAlias(message, alias);
	}
}
