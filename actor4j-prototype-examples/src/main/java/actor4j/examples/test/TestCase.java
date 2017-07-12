/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.examples.test;

import static actor4j.core.utils.ActorLogger.logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.testing.ActorTest;
import actor4j.testing.TestSystem;
import bdd4j.Story;
import static bdd4j.OutcomeFactory.*;

public class TestCase {
	protected static class MyActor extends Actor implements ActorTest {
		@Override
		public void receive(ActorMessage<?> message) {
			if (message.value instanceof String) {
				logger().info(String.format(
						"Received String message: %s", message.valueAsString()));
				tell("Hello World Again!", 0, message.source);
			} 
			else
				unhandled(message);
		}
		
		@Override
		public List<Story> test() {
			List<Story> result = new LinkedList<>();
			
			MutableObject<String> actual = new MutableObject<>();
			
			Story story = new Story();
			story.scenario()
				.annotate("Sceneario: Sending and receiving a message String")
				.annotate("Given a request message")
				.given(() -> {
					send(new ActorMessage<String>("Hello World!", 0, getSystem().SYSTEM_ID, self()));
				})
				.annotate("When the responded message was received")
				.when(()-> {
					try {
						ActorMessage<?> message = ((TestSystem)getSystem()).awaitMessage(5, TimeUnit.SECONDS);
						actual.setValue(message.valueAsString());
					} catch (InterruptedException | TimeoutException e) {
						e.printStackTrace();
					}
					
					((TestSystem)getSystem()).assertNoMessages();
				})
				.annotate("Then the responded received String message will be \"Hello World Again!\"")
				.then(() -> {
					outcome(actual.getValue()).shouldBe("Hello World Again!");
				});
			
			result.add(story);
			
			return result;
		}
	}
	
	protected TestSystem system;
	
	@Before
	public void before() {
		system = new TestSystem();
		
		system.addActor(() -> new MyActor());

		system.start();
	}
	
	@Test
	public void test() {
		system.testAllActors();
	}
	
	@After
	public void after() {
		system.shutdownWithActors(true);
	}
	
	public static void main(String... args) {
		TestCase testCase = new TestCase();
		testCase.before();
		testCase.test();
		testCase.after();
	}
}
