/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
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
package io.actor4j.core.actors.behaviour;

import io.actor4j.core.messages.ActorMessage;

// behaviour: handles a message and returns how to go on (same, unhandled, stopped or the next behaviour)
@FunctionalInterface
public interface ActorBehaviour {
	public ActorBehaviour handle(ActorMessage<?> message);

	// name of the behaviour (state), null if unnamed
	public default String name() {
		return null;
	}

	// stay with the current behaviour
	public static ActorBehaviour same() {
		return Marker.SAME;
	}

	// message not handled: unhandled(message) of the actor
	public static ActorBehaviour unhandled() {
		return Marker.UNHANDLED;
	}

	// stop the actor
	public static ActorBehaviour stopped() {
		return Marker.STOPPED;
	}
	
	// back to the initial behaviour of the actor (for behaviours that do not know their actor, e.g. await)
	public static ActorBehaviour initial() {
		return Marker.INITIAL;
	}

	// behaviour where every message is unhandled
	public static ActorBehaviour empty() {
		return Basic.EMPTY;
	}

	// behaviour that silently drops every message (same, no unhandled)
	public static ActorBehaviour ignore() {
		return Basic.IGNORE;
	}

	public static ActorBehaviour named(String name, ActorBehaviour behaviour) {
		return new ActorBehaviour() {
			@Override
			public ActorBehaviour handle(ActorMessage<?> message) {
				return behaviour.handle(message);
			}

			@Override
			public String name() {
				return name;
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	// real behaviours (state names "EMPTY" and "IGNORE")
	enum Basic implements ActorBehaviour {
		EMPTY {
			@Override
			public ActorBehaviour handle(ActorMessage<?> message) {
				return ActorBehaviour.unhandled();
			}
		},
		IGNORE {
			@Override
			public ActorBehaviour handle(ActorMessage<?> message) {
				return ActorBehaviour.same();
			}
		}
	}

	enum Marker implements ActorBehaviour {
		SAME, UNHANDLED, STOPPED, INITIAL;

		@Override
		public ActorBehaviour handle(ActorMessage<?> message) {
			throw new UnsupportedOperationException(name()+" is a marker, not a behaviour");
		}
	}
}
