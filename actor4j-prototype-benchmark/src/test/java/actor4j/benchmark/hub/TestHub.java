/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.hub;

import static actor4j.benchmark.hub.ActorMessageTag.RUN;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class TestHub {
	public TestHub() {
		ActorSystem system = new ActorSystem("actor4j::TestHub");
		//system.setParallelismFactor(1);
		system.setParallelismMin(1);
		system.softMode();
		
		
		UUID dest = system.addActor(Destination.class);
		ActorGroup group = new ActorGroup();
		int size = 100;
		UUID id = null;
		for(int i=0; i<size; i++) {
			id = system.addActor(Client.class, dest);
			group.add(id);
		}
		
		system.broadcast(new ActorMessage<Object>(new Object(), RUN, dest, null), group);
		
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestHub();
	}
}
