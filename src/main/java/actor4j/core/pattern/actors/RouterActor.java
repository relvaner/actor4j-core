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
package actor4j.core.pattern.actors;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorMessageMatcher;

public class RouterActor extends Actor {
	protected ActorMessageMatcher matcher;
	
	public RouterActor(List<RouteeHandler> routees) {
		this(null, routees);
	}
	
	public RouterActor(String name, List<RouteeHandler> routees) {
		super(name);
		
		matcher = new ActorMessageMatcher();
		
		for (RouteeHandler handler : routees)
			add(handler.getPredicate(), handler.getRoutee());
		matcher.matchElse(msg -> unhandled(msg));
	}

	@Override
	public void receive(ActorMessage<?> message) {
		matcher.apply(message);
	}
	
	public void add(Predicate<ActorMessage<?>> predicate, UUID routee) {
		matcher.match(predicate, (msg) -> forward(msg, routee));
	}
}
