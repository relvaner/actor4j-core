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
package io.actor4j.core.features.actor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.actor4j.core.actors.Actor;
import io.actor4j.core.messages.ActorMessage;

public class ReflectionActor extends Actor {
	public static CountDownLatch testDone = new CountDownLatch(1);
	
	public int value1;
	public String value2;
	public boolean value3;
	public List<String> value4;
	public Integer value5;
	public Map<Integer, String> value6;
	public double value7;
	
	public ReflectionActor(int value1, String value2, boolean value3, List<String> value4, Integer value5) {
		super();
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
		this.value5 = value5;
	}
	
	public ReflectionActor(int value1, String value2) {
		super();
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public ReflectionActor(Map<Integer, String> value6, double value7) {
		super();
		this.value6 = value6;
		this.value7 = value7;
	}
	
	@Override
	public void preStart() {
		testDone.countDown();
	}

	@Override
	public void receive(ActorMessage<?> message) {
		// empty
	}
}
