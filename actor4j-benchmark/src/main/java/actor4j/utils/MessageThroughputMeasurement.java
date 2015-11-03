/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.utils;

import java.text.DecimalFormat;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import actor4j.function.Supplier;
import tools4j.utils.Timer;
import tools4j.utils.TimerListener;

public class MessageThroughputMeasurement {
	protected Timer timer;
	
	public MessageThroughputMeasurement(final Supplier<Long> counter, final DescriptiveStatistics statistics, final boolean console) {
		final DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
		
		timer = new Timer(1000, new TimerListener() {
			protected int iteration; 
			protected long lastCount;
			@Override
			public void task() {
				long count = counter.get();
				long diff  = count-lastCount;
				
				if (statistics!=null && iteration>0)
					statistics.addValue(diff);
				if (console && iteration>0)
					System.out.printf("%-2d : %s msg/s%n", iteration, decimalFormat.format(diff));
				
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
