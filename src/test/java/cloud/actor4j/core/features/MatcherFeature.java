/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
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
package cloud.actor4j.core.features;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorMessageMatcher;

public class MatcherFeature {
	protected ActorMessageMatcher matcher;
	
	protected int[] postconditions = new int[3];
	
	@Before
	public void before() {
		matcher = new ActorMessageMatcher();
		
		matcher
			.match(UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[2] = 2134;
				}
			})
			.match(new UUID[] {UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"), UUID.fromString("218e064b-0b92-4f80-95b3-49002d1e9b46")}, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[2] = 2199;
				}
			})
			.match(new int[] {-8, 10}, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[0] = message.tag;
				}
			})		
			.match(UUID.fromString("4cd883bb-b2b0-4e98-bf53-416e38334a12"), 22, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[2] = 187;
				}
			})	
			.match(new UUID[] {UUID.fromString("26899da9-a339-41be-9969-e730fd880cae"), UUID.fromString("18a06a5f-b227-452e-a948-908f4e619074")}, 25, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[2] = 195;
				}
			})	
			.match(UUID.fromString("7ed0c429-5536-4ee8-9c84-52384cb24047"), new int[] {127, 129}, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[2] = 1872;
				}
			})	
			.match(new UUID[] {UUID.fromString("e6ce694f-653e-4965-b1ba-c0a0166eff92"), UUID.fromString("720776f3-93d0-45b7-b0af-58465c2a4ac0")}, new int[] {327, 329}, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[2] = 18725;
				}
			})	
			// .match(-5, (msg) -> postcondition=msg.tag)
			.match(-5, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[0] = message.tag;
				}
			})			
			.matchAny(new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[1] = message.tag;
				}
			})
			.match(15, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[0] = message.tag;
				}
			})
			.matchElse(new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[0] = message.tag;
				}
			})
			.match(String.class, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[0] = 1976;
				}
			})
			.match(String.class, 
					new Predicate<ActorMessage<?>>() {
						@Override
						public boolean test(ActorMessage<?> message) {
							return message.valueAsString().equals("Hello World!");
						}
					}, 
					new Consumer<ActorMessage<?>>() {
						@Override
						public void accept(ActorMessage<?> message) {
							postconditions[0] = 1976+1;
						}
			})
			.match(17, new Consumer<ActorMessage<?>>() {
				@Override
				public void accept(ActorMessage<?> message) {
					postconditions[0] = message.tag;
				}
			});
	}
	
	@Test
	public void test() {
		matcher.apply(new ActorMessage<Object>(null, -5, UUID.fromString("5ae55fff-d420-4c31-bbe7-0b18812766c2"), null));
		assertEquals(2134, postconditions[2]);
		assertEquals(-5, postconditions[0]);
		assertEquals(-5, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, -5, UUID.fromString("acefb4a4-b2a0-4641-8553-9a0ac12e282a"), null));
		assertEquals(2199, postconditions[2]);
		assertEquals(-5, postconditions[0]);
		assertEquals(-5, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, -5, UUID.fromString("218e064b-0b92-4f80-95b3-49002d1e9b46"), null));
		assertEquals(2199, postconditions[2]);
		assertEquals(-5, postconditions[0]);
		assertEquals(-5, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, -8, null, null));
		assertEquals(-8, postconditions[0]);
		assertEquals(-8, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 10, null, null));
		assertEquals(10, postconditions[0]);
		assertEquals(10, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 22, UUID.fromString("4cd883bb-b2b0-4e98-bf53-416e38334a12"), null));
		assertEquals(187, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(22, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 25, UUID.fromString("26899da9-a339-41be-9969-e730fd880cae"), null));
		assertEquals(195, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(25, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 25, UUID.fromString("18a06a5f-b227-452e-a948-908f4e619074"), null));
		assertEquals(195, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(25, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 127, UUID.fromString("7ed0c429-5536-4ee8-9c84-52384cb24047"), null));
		assertEquals(1872, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(127, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 129, UUID.fromString("7ed0c429-5536-4ee8-9c84-52384cb24047"), null));
		assertEquals(1872, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(129, postconditions[1]);
		
		matcher.apply(new ActorMessage<Object>(null, 327, UUID.fromString("e6ce694f-653e-4965-b1ba-c0a0166eff92"), null));
		assertEquals(18725, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(327, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 329, UUID.fromString("e6ce694f-653e-4965-b1ba-c0a0166eff92"), null));
		assertEquals(18725, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(329, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 327, UUID.fromString("720776f3-93d0-45b7-b0af-58465c2a4ac0"), null));
		assertEquals(18725, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(327, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 329, UUID.fromString("720776f3-93d0-45b7-b0af-58465c2a4ac0"), null));
		assertEquals(18725, postconditions[2]);
		assertEquals(10, postconditions[0]);
		assertEquals(329, postconditions[1]);
		
		matcher.apply(new ActorMessage<Object>(null, -5, null, null));
		assertEquals(-5, postconditions[0]);
		assertEquals(-5, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 15, null, null));
		assertEquals(15, postconditions[0]);
		assertEquals(15, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 17, null, null));
		assertEquals(17, postconditions[0]);
		assertEquals(17, postconditions[1]);
		matcher.apply(new ActorMessage<Object>(null, 235, null, null));
		assertEquals(235, postconditions[0]);
		assertEquals(235, postconditions[1]);
		matcher.apply(new ActorMessage<Object>("", 235, null, null));
		assertEquals(1976, postconditions[0]);
		assertEquals(235, postconditions[1]);
		matcher.apply(new ActorMessage<Object>("Hello World!", 235, null, null));
		assertEquals(1976+1, postconditions[0]);
		assertEquals(235, postconditions[1]);
	}
}
