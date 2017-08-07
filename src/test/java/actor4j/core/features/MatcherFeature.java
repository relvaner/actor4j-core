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
package actor4j.core.features;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorMessageMatcher;

public class MatcherFeature {
	protected ActorMessageMatcher matcher;
	
	protected int[] postconditions = new int[2];
	
	@Before
	public void before() {
		matcher = new ActorMessageMatcher();
		
		matcher
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
