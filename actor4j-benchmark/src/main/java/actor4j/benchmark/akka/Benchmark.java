/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.akka;

import java.text.DecimalFormat;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import actor4j.function.Supplier;
import actor4j.utils.MessageThroughputMeasurement;
import akka.actor.ActorSystem;

public class Benchmark {
	protected ActorSystem system;
	protected Supplier<Long> counter;
	protected long duration;
	
	public Benchmark(ActorSystem system, Supplier<Long> counter, long duration) {
		super();
		
		this.system = system;
		this.counter = counter;
		this.duration = duration;
	}
	
	public void start() {
		DescriptiveStatistics statistics = new DescriptiveStatistics();
		DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###");
		
		System.out.println("Benchmark started...");

		MessageThroughputMeasurement messageTM = new MessageThroughputMeasurement(counter, statistics, true);
		messageTM.start();
		
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		messageTM.stop();
		system.shutdown();
		system.awaitTermination();
		
		System.out.printf("statistics::count        : %s%n", decimalFormat.format(counter.get()));
		System.out.printf("statistics::mean::exact  : %s msg/s%n", decimalFormat.format(counter.get()/(duration/1000)));
		System.out.printf("statistics::mean         : %s msg/s%n", decimalFormat.format(statistics.getMean()));
		System.out.printf("statistics::sd           : %s msg/s%n", decimalFormat.format(statistics.getStandardDeviation()));
		System.out.printf("statistics::median       : %s msg/s%n", decimalFormat.format(statistics.getPercentile(50)));
		System.out.println("Benchmark finished...");
			
	}
}