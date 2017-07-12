/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ping.pong.grouped;

import static actor4j.benchmark.ping.pong.grouped.ActorMessageTag.MSG;

import java.util.UUID;

import actor4j.benchmark.Benchmark;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorGroup;

public class TestPingPong {
	public TestPingPong() {
		ActorSystem system = new ActorSystem("actor4j::PingPong-Grouped");
		//system.setParallelismFactor(1);
		//system.setParallelismMin(1);
		system.softMode();
		
		ActorGroup group = new ActorGroup();
		ActorGroup[] groups = new ActorGroup[4];
		for (int i=0; i<groups.length; i++)
			groups[i] = new ActorGroup();
		int size = 400;
		UUID dest = null;
		UUID id = null;
		for(int i=0; i<size; i++) {
			dest = system.addActor(Destination.class, groups[i%4]);
			id = system.addActor(Client.class, groups[(i+1)%4], dest);
			group.add(id);
		}
		
		system.broadcast(new ActorMessage<Object>(new Object(), MSG, dest, null), group);
		
		Benchmark benchmark = new Benchmark(system, 60000);
		benchmark.start();
	}
	
	public static void main(String[] args) {
		new TestPingPong();
	}
}
