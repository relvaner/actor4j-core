/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.bcast2;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;
import actor4j.core.utils.ActorGroupAsList;

public class TestBcast2 {
	public TestBcast2() {
		ActorSystem system = new ActorSystem("actor4j::Bcast2");
		system.setParallelismFactor(1);
		//system.setParallelismMin(1);
		system.softMode();
		
		ActorGroupAsList group = new ActorGroupAsList();
		int size = 100;
		UUID id = null;
		for(int i=0; i<size; i++) {
			id = system.addActor(TestActor.class, group);
			group.add(id);
		}
		
		system.broadcast((new ActorMessage<Object>(new Object(), 0, id, null)), new ActorGroup(group));
		
		
		Benchmark benchmark = new Benchmark(system, 30000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestBcast2();
	}
}
