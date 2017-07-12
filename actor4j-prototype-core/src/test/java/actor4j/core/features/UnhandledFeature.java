/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.core.features;

import java.util.UUID;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;

import static actor4j.core.utils.ActorLogger.logger;

public class UnhandledFeature {
	protected ActorSystem system;

	@Before
	public void before() {
		system = new ActorSystem();
		system.setParallelismMin(1);
		system.setDebugUnhandled(true);
	}
	
	@Test
	public void test() {
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
					}
				};
			}
		});
		
		system.send(new ActorMessage<Object>(null, 0, system.SYSTEM_ID, dest));
		system.start();
		try {
			Thread.sleep(100);
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
