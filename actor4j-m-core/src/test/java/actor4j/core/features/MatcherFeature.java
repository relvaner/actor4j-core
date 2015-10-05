/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.features;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import actor4j.core.ActorMessage;
import actor4j.core.ActorMessageMatcher;
import actor4j.function.Consumer;

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
	}
}
