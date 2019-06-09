/*
 * Copyright (c) 2015-2018, David A. Bauer. All rights reserved.
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

import org.junit.Test;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.immutable.ImmutableList;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroup;
import io.actor4j.core.utils.ActorGroupSet;
import io.actor4j.core.utils.CommPattern;
import io.actor4j.core.utils.Range;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class CommPatternFeature {
	@Test
	public void test() {
		int size = 5;
		int arr_size = 20;
		Range range = CommPattern.loadBalancing(0, size, arr_size);
		assertEquals(new Range(0, 3), range);
		for (int rank=0; rank<size; rank++) {
			range = CommPattern.loadBalancing(rank, size, arr_size);
			assertEquals(new Range(rank*4, rank*4+3), range);
		}
		range = CommPattern.loadBalancing(1, size, arr_size);
		assertEquals(new Range(4, 7), range);
		range = CommPattern.loadBalancing(2, size, arr_size);
		assertEquals(new Range(8, 11), range);
		range = CommPattern.loadBalancing(3, size, arr_size);
		assertEquals(new Range(12, 15), range);
		range = CommPattern.loadBalancing(4, size, arr_size);
		assertEquals(new Range(16, 19), range);
		
		size = 5;
		arr_size = 21;
		range = CommPattern.loadBalancing(0, size, arr_size);
		assertEquals(new Range(0, 4), range);
		for (int rank=1; rank<size; rank++) {
			range = CommPattern.loadBalancing(rank, size, arr_size);
			assertEquals(new Range(rank*4+1, rank*4+4), range);
		}
		range = CommPattern.loadBalancing(1, size, arr_size);
		assertEquals(new Range(5, 8), range);
		range = CommPattern.loadBalancing(2, size, arr_size);
		assertEquals(new Range(9, 12), range);
		range = CommPattern.loadBalancing(3, size, arr_size);
		assertEquals(new Range(13, 16), range);
		range = CommPattern.loadBalancing(4, size, arr_size);
		assertEquals(new Range(17, 20), range);
		
		size = 5;
		arr_size = 22;
		range = CommPattern.loadBalancing(0, size, arr_size);
		assertEquals(new Range(0, 4), range);
		range = CommPattern.loadBalancing(1, size, arr_size);
		assertEquals(new Range(5, 9), range);
		for (int rank=2; rank<size; rank++) {
			range = CommPattern.loadBalancing(rank, size, arr_size);
			assertEquals(new Range(rank*4+2, rank*4+5), range);
		}
		range = CommPattern.loadBalancing(2, size, arr_size);
		assertEquals(new Range(10, 13), range);
		range = CommPattern.loadBalancing(3, size, arr_size);
		assertEquals(new Range(14, 17), range);
		range = CommPattern.loadBalancing(4, size, arr_size);
		assertEquals(new Range(18, 21), range);
	}
	
	@Test(timeout=5000)
	public void test_scatter() {
		CountDownLatch testDone = new CountDownLatch(1);
		
		ActorSystem system = new ActorSystem();
		
		UUID parent = system.addActor(() -> new Actor("parent") {
			protected UUID child;
			@Override
			public void preStart() {
				child = addChild(() -> new Actor("child") {
					@Override
					public void receive(ActorMessage<?> message) {
						@SuppressWarnings("unchecked")
						ImmutableList<String> list = (ImmutableList<String>)message.value;
						assertEquals("Hello", list.get().get(0));
						assertEquals("World!", list.get().get(1));
						testDone.countDown();
					}
				});
			}
			
			@Override
			public void receive(ActorMessage<?> message) {
				ActorGroup group = new ActorGroupSet();
				group.add(child);
				List<String> list = new ArrayList<>();
				list.add("Hello");
				list.add("World!");
				CommPattern.scatter(list, 0, this, group);
			}
		});
		
		system.start();
		system.send(new ActorMessage<>(null, 0, system.SYSTEM_ID, parent));
		try {
			testDone.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		system.shutdownWithActors(true);
	}
}
