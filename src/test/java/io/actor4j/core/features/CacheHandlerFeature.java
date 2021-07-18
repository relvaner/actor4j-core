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
import java.util.function.Consumer;

import org.junit.Test;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorCacheHandler;
import io.actor4j.core.utils.Pair;

public class CacheHandlerFeature {
	protected boolean postcondition;
	
	@Test
	public void test_match() {
		ActorCacheHandler<String, Integer> handler = new ActorCacheHandler<>((msg) -> Pair.of(msg.value.toString(), 42));
		Consumer<Pair<String, Integer>> action = (pair) -> {
			if (pair.a.equals("Hello World!") && pair.b.equals(42))
				postcondition = true;
		};
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertTrue(handler.match(new ActorMessage<>("Hello World!", 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertTrue(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(new ActorMessage<>("Hello World!", 0, null, null, UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"))));
		assertFalse(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertTrue(handler.match(new ActorMessage<>(42, 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
		
		
		handler = new ActorCacheHandler<>((msg) -> null);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(new ActorMessage<>("Hello World!", 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(new ActorMessage<>("Hello World!", 0, null, null, UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"))));
		assertFalse(postcondition);
		
		postcondition = false;
		handler.define(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), action);
		assertFalse(handler.match(new ActorMessage<>(42, 0, null, null, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"))));
		assertFalse(postcondition);
	}
}
