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
package io.actor4j.core.features;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.junit.Test;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorMessageHandler;

public class HandlerFeature {
	protected boolean postcondition;
	protected boolean postcondition_null;
	
	@Test
	public void test_match() {
		ActorMessageHandler<String> handler = new ActorMessageHandler<>(String.class);
		BiConsumer<String, Integer> action = (value, tag) -> {
			if (value.equals("Hello World!"))
				postcondition = true;
		};
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertTrue(handler.match(ActorMessage.create("Hello World!", 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertTrue(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(ActorMessage.create("Hello World!", 0, null, null, UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"))));
		assertFalse(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(ActorMessage.create(42, 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
	}
	
	@Test
	public void test_predicate() {
		ActorMessageHandler<String> handler = new ActorMessageHandler<>(String.class, (msg) -> msg.domain().equals("domainA"));
		BiConsumer<String, Integer> action = (value, tag) -> {
			if (value.equals("Hello World!"))
				postcondition = true;
		};
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertTrue(handler.match(ActorMessage.create("Hello World!", 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), "", "domainA")));
		assertTrue(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(ActorMessage.create("Hello World!", 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), "", "domainB")));
		assertFalse(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(ActorMessage.create(42, 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
	}
	
	@Test
	public void test_matchOfNullable() {
		ActorMessageHandler<String> handler = new ActorMessageHandler<>(String.class);
		BiConsumer<String, Integer> action = (value, tag) -> {
			if (value!=null) {
				if (value.equals("Hello World!"))
					postcondition = true;
			}
			else
				postcondition_null = true;
		};
		
		postcondition = false;
		postcondition_null = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertTrue(handler.matchOfNullable(ActorMessage.create("Hello World!", 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertTrue(postcondition);
		assertFalse(postcondition_null);

		postcondition = false;
		postcondition_null = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.matchOfNullable(ActorMessage.create("Hello World!", 0, null, null, UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"))));
		assertFalse(postcondition);
		assertFalse(postcondition_null);
		
		postcondition = false;
		postcondition_null = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.matchOfNullable(ActorMessage.create(42, 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
		assertFalse(postcondition_null);

		postcondition = false;
		postcondition_null = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertTrue(handler.matchOfNullable(ActorMessage.create(null, 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
		assertTrue(postcondition_null);
		
		postcondition = false;
		postcondition_null = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.matchOfNullable(ActorMessage.create(null, 0, null, null, UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"))));
		assertFalse(postcondition);
		assertFalse(postcondition_null);
	}
}
