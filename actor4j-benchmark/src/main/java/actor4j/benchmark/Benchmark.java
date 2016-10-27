/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import actor4j.core.ActorSystem;
import actor4j.core.DefaultActorSystemImpl;
import actor4j.function.Supplier;
import actor4j.utils.MessageThroughputMeasurement;

public class Benchmark {
	protected ActorSystem system;
	protected long duration;
	protected long warmupIterations;
	
	public Benchmark(ActorSystem system, long duration) {
		this(system, 10, duration);
	}
	
	public Benchmark(ActorSystem system, long warmupIterations, long duration) {
		super();
		
		this.system = system;
		this.warmupIterations = warmupIterations;
		this.duration = duration;
	}
	
	public void start() {
		final DescriptiveStatistics statistics = new DescriptiveStatistics();
		final AtomicLong warmupCount = new AtomicLong();
		
		System.out.printf("Logical cores: %d%n", Runtime.getRuntime().availableProcessors());
		System.out.printf("Benchmark started (%s)...%n", system.getName());
		system.underlyingImpl().setCounterEnabled(true);
		system.start(new Runnable() {
			@Override
			public void run() {
				DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###");
				
				int i=0;
				for (long value : system.underlyingImpl().getExecuterService().getCounts()) {
					System.out.printf("actor4j-worker-thread-%d::count = %s%n", i, decimalFormat.format(value));
					i++;
				}
				if (system.underlyingImpl() instanceof DefaultActorSystemImpl) {
					i=0;
					for (int value : ((DefaultActorSystemImpl)system.underlyingImpl()).getWorkerInnerQueueSizes()) {
						System.out.printf("actor4j-worker-thread-%d::inner::queue::size = %s%n", i, decimalFormat.format(value));
						i++;
					}
					i=0;
					for (int value : ((DefaultActorSystemImpl)system.underlyingImpl()).getWorkerOuterQueueSizes()) {
						System.out.printf("actor4j-worker-thread-%d::outer::queue::size = %s%n", i, decimalFormat.format(value));
						i++;
					}
				}
				System.out.printf("statistics::count         : %s%n", decimalFormat.format(system.underlyingImpl().getExecuterService().getCount()-warmupCount.get()));
				System.out.printf("statistics::mean::derived : %s msg/s%n", decimalFormat.format((system.underlyingImpl().getExecuterService().getCount()-warmupCount.get())/(duration/1000)));
				System.out.printf("statistics::mean          : %s msg/s%n", decimalFormat.format(statistics.getMean()));
				System.out.printf("statistics::sd            : %s msg/s%n", decimalFormat.format(statistics.getStandardDeviation()));
				System.out.printf("statistics::median        : %s msg/s%n", decimalFormat.format(statistics.getPercentile(50)));
				System.out.println("Benchmark finished...");
			}
		});
		
		MessageThroughputMeasurement messageTM = new MessageThroughputMeasurement(new Supplier<Long>() {
			@Override
			public Long get() {
				return system.underlyingImpl().getExecuterService().getCount();
			}
		}, warmupIterations, warmupCount, statistics, true);
		messageTM.start();
		
		try {
			Thread.sleep(duration+warmupIterations*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		messageTM.stop();
		system.shutdown();
	}
}