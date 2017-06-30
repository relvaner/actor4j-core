/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ejb;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class Pong {
	@EJB
	Ping ping;
	
	@Asynchronous
	public void receive(ActorMessage message) {
		Benchmark.counter.incrementAndGet();
		ping.receive(new ActorMessage(new Object(), 0));
	}
}
