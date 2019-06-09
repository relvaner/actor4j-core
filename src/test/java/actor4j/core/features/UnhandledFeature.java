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

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static cloud.actor4j.core.utils.ActorLogger.logger;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import cloud.actor4j.core.ActorSystem;
import cloud.actor4j.core.actors.Actor;
import cloud.actor4j.core.messages.ActorMessage;
import cloud.actor4j.core.utils.ActorFactory;

public class UnhandledFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
		system.setDebugUnhandled(true);
	}
	
	@Test(timeout=5000)
	public void test() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		UUID dest = system.addActor(new ActorFactory() { 
			@Override
			public Actor create() {
				return new Actor("UnhandledFeatureActor") {
					@Mock
					protected Appender mockAppender;
					@Captor
					protected ArgumentCaptor<LoggingEvent> captorLoggingEvent;
					
					@Override
					public void receive(ActorMessage<?> message) {
						MockitoAnnotations.initMocks(this);
						logger().removeAllAppenders();
						logger().addAppender(mockAppender);
						unhandled(message);
						verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
						LoggingEvent loggingEvent = captorLoggingEvent.getValue();
						assertTrue(loggingEvent.getMessage().toString().contains("Unhandled message"));
						testDone.countDown();
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		system.shutdown(true);
	}
	
	public static void main(String[] args) {
		UnhandledFeature unhandledFeature = new UnhandledFeature();
		unhandledFeature.before();
		unhandledFeature.test();
	}
}
