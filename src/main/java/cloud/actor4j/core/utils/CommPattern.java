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
package cloud.actor4j.core.utils;

import java.util.List;
import java.util.UUID;

import cloud.actor4j.core.actors.ActorRef;
import cloud.actor4j.core.immutable.ImmutableList;
import cloud.actor4j.core.messages.ActorMessage;
import cloud.actor4j.core.utils.Range;

public final class CommPattern {
	public static Range loadBalancing(int rank, int size, int arr_size) {
		int quotient  = arr_size/size;
		int remainder = arr_size%size;
		
		int low  = 0;
		int high = 0;
	
		if (rank<remainder) {
			low  = rank * quotient + rank;
			high = low + quotient;
		}
		else {
			low  = rank * quotient + remainder ;
			high = low + quotient - 1;
		}
		
		return new Range(low, high);
	}
	
	public static void broadcast(ActorMessage<?> message, ActorRef actorRef, ActorGroup group) {
		for (UUID dest : group)
			actorRef.send(message, dest);
	}
	
	public static <T> void scatter(List<T> list, int tag, ActorRef actorRef, ActorGroup group) {
		int i=0;
		for (UUID dest : group) {
			Range range = loadBalancing(i, group.size(), list.size());
			actorRef.send(new ActorMessage<>(new ImmutableList<T>(list.subList(range.low, range.high+1)), tag, actorRef.self(), dest));	
			i++;
		}
	}
}
