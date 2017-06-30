/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.ejb;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class Benchmark {
	@EJB
	Ping ping;
	
	protected long iteration;
	protected long lastCount;
	protected DecimalFormat decimalFormat;
	
	public static AtomicLong counter = new AtomicLong();
	
	public Benchmark() {
		iteration = 1;
		decimalFormat = new DecimalFormat("###,###,###");
	}
	
	@PostConstruct
	void preStart() {
		ping.receive(new ActorMessage(new Object(), 0));
	}
	
	@Schedule(second="*/1", minute="*",hour="*", persistent=false)
    public void schedule(){
		long count = counter.get();
		long diff  = count-lastCount;
		
		System.out.printf("%-2d : %s msg/s%n", ++iteration, decimalFormat.format(diff));
		
		lastCount = count;
    }
}
