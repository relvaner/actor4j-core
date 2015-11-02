/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark;

import java.text.DecimalFormat;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import actor4j.core.ActorSystem;
import actor4j.core.mono.MonoActorSystemImpl;
import actor4j.function.Supplier;
import actor4j.utils.MessageThroughputMeasurement;

public class Benchmark {
	protected ActorSystem system;
	protected long duration;
	
	public Benchmark(ActorSystem system, long duration) {
		super();
		
		this.system = system;
		this.duration = duration;
	}
	
	public void start() {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();
		
		System.out.println("Benchmark started...");
		system.start(new Runnable() {
			@Override
			public void run() {
				DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###");
				
				int i=0;
				for (long value : system.underlyingImpl().getExecuterService().getCounts()) {
					System.out.printf("worker-%d::count = %s%n", i, decimalFormat.format(value));
					i++;
				}
				i=0;
				for (int value : ((MonoActorSystemImpl)system.underlyingImpl()).getWorkerInnerQueueSizes()) {
					System.out.printf("worker-%d::inner::queue::size = %s%n", i, decimalFormat.format(value));
					i++;
				}
				i=0;
				for (int value : ((MonoActorSystemImpl)system.underlyingImpl()).getWorkerOuterQueueSizes()) {
					System.out.printf("worker-%d::outer::queue::size = %s%n", i, decimalFormat.format(value));
					i++;
				}
				System.out.printf("statistics::count        : %s%n", decimalFormat.format(system.underlyingImpl().getExecuterService().getCount()));
				System.out.printf("statistics::mean::exact  : %s msg/s%n", decimalFormat.format(system.underlyingImpl().getExecuterService().getCount()/(duration/1000)));
				System.out.printf("statistics::mean         : %s msg/s%n", decimalFormat.format(statistics.getMean()));
				System.out.printf("statistics::sd           : %s msg/s%n", decimalFormat.format(statistics.getStandardDeviation()));
				System.out.printf("statistics::median       : %s msg/s%n", decimalFormat.format(statistics.getPercentile(50)));
				System.out.println("Benchmark finished...");
			}
		});
		
		MessageThroughputMeasurement messageTM = new MessageThroughputMeasurement(new Supplier<Long>() {
			@Override
			public Long get() {
				return system.underlyingImpl().getExecuterService().getCount();
			}
		}, statistics, true);
		messageTM.start();
		
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		messageTM.stop();
		system.shutdown();
	}
}