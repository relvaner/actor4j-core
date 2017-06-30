/*
 * Copyright (c) 2015-2017, David A. Bauer. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */
package actor4j.benchmark.utils;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import tools4j.utils.Timer;
import tools4j.utils.TimerListener;

public class MessageThroughputMeasurement {
	protected Timer timer;

	public MessageThroughputMeasurement(final Supplier<Long> counter, final long warmupIterations, final AtomicLong warmupCount, final DescriptiveStatistics statistics, final boolean console) {
		final DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
		
		timer = new Timer(1000, new TimerListener() {
			protected int iteration = 1; 
			protected long lastCount;
			@Override
			public void task() {
				long count = counter.get();
				long diff  = count-lastCount;
				
				if (statistics!=null && iteration>warmupIterations)
					statistics.addValue(diff);
				else
					warmupCount.set(count);
				if (console && iteration>warmupIterations)
					System.out.printf("%-2d : %s msg/s%n", iteration-warmupIterations, decimalFormat.format(diff));
				else
					System.out.printf("Warmup %-2d : %s msg/s%n", iteration, decimalFormat.format(diff));
				
				lastCount = count;
				iteration++;
			}
		});
	}

	public void start() {
		timer.start();
	}
	
	public void stop() {
		timer.interrupt();
	}
}
