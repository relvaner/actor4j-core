/*
 * Copyright (c) 2015-2026, David A. Bauer. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.actor4j.core.benchmark;

import static io.actor4j.core.logging.ActorLogger.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import io.actor4j.core.ActorSystem;
import io.actor4j.core.ClassicActorRuntime;
import io.actor4j.core.DefaultActorRuntime;
import io.actor4j.core.HybridActorRuntime;
import io.actor4j.core.actors.Actor;
import io.actor4j.core.actors.ActorWithGroup;
import io.actor4j.core.actors.PooledActor;
import io.actor4j.core.config.ActorSystemConfig;
import io.actor4j.core.config.HybridActorSystemConfig;
import io.actor4j.core.id.ActorId;
import io.actor4j.core.messages.ActorMessage;
import io.actor4j.core.utils.ActorGroupSet;

// AI-generated.
// Runtime benchmark: default vs. classic vs. hybrid (rings thread-bound, fib pooled).
// Not a unit test (no *Test name), run manually: without arguments the whole matrix is executed,
// each configuration in its own JVM (no JIT profile pollution by other runtimes).
// Single run: <runtime> <workload> <threads>
//   runtime:  default, classic, hybrid
//   workload: ring  (one ring of 100 actors per thread, a group per ring: steady load with locality)
//             fib   (fib(22) with actors: fan-out/fan-in, about 57,000 actors)
//             mixed (rings run continuously while 20 x fib(18) are computed: fib latency and ring throughput)
//             scatter (IoT style: 2,000 device actors with circulating telemetry, thread-bound; 50 concurrent queries,
//                     each a coordinator (pooled in hybrid) asking 200 random devices: query latency and telemetry rate)
public class HybridBenchmark {
	protected static final int WARMUP = 3;
	protected static final int ITERATIONS = 7;

	protected static final int HOP = 1, CREATE = 2, RESULT = 3, TELEMETRY = 4, QUERY = 5, ANSWER = 6, START = 7;
	protected static final int RING_SIZE = 100;
	protected static int pooled; // hybrid: pooledParallelism, 0 = automatic (5th argument)

	protected static ActorSystem create(String runtime, int threads) {
		return switch (runtime) {
			case "default" -> DefaultActorRuntime.create(ActorSystemConfig.builder().parallelism(threads).build());
			case "classic" -> ClassicActorRuntime.create(ActorSystemConfig.builder().parallelism(threads).build());
			case "hybrid"  -> HybridActorRuntime.create(pooled>0
				? HybridActorSystemConfig.builder().pooledParallelism(pooled).parallelism(threads).build()
				: HybridActorSystemConfig.builder().parallelism(threads).build());
			default -> throw new IllegalArgumentException(runtime);
		};
	}

	// ring: forwarders and one sender in the same group (default/hybrid: same thread)
	protected static class Forwarder extends ActorWithGroup {
		protected final ActorId next;

		public Forwarder(ActorGroupSet group, ActorId next) {
			super(group);
			this.next = next;
		}

		@Override
		public void receive(ActorMessage<?> message) {
			tell(message.value(), HOP, next);
		}
	}

	protected static class Sender extends ActorWithGroup {
		protected ActorId first;
		protected final CountDownLatch done;
		protected final LongAdder rounds;
		protected final boolean[] stop;

		public Sender(ActorGroupSet group, CountDownLatch done, LongAdder rounds, boolean[] stop) {
			super(group);
			this.done = done;
			this.rounds = rounds;
			this.stop = stop;
		}

		@Override
		public void receive(ActorMessage<?> message) {
			if (message.tag()==CREATE) {
				first = (ActorId)message.value();
				return;
			}
			int left = (Integer)message.value();
			rounds.increment();
			if (left>0 && !stop[0])
				tell(left-1, HOP, first);
			else
				done.countDown();
		}
	}

	protected static List<ActorId> rings(ActorSystem system, int threads, CountDownLatch done, LongAdder rounds, boolean[] stop) {
		List<ActorId> senders = new ArrayList<>();
		for (int r=0; r<threads; r++) {
			ActorGroupSet group = new ActorGroupSet();
			ActorId sender = system.addActor(() -> new Sender(group, done, rounds, stop));
			ActorId next = sender;
			for (int i=0; i<RING_SIZE-1; i++) {
				final ActorId next_ = next;
				next = system.addActor(() -> new Forwarder(group, next_));
			}
			system.send(ActorMessage.create(next, CREATE, system.SYSTEM_ID(), sender));
			senders.add(sender);
		}
		return senders;
	}

	// fib: two children per actor, results flow back to the parent
	protected static class FibActor extends Actor {
		protected final long n;
		protected final CompletableFuture<Long> future; // root only
		protected long sum;
		protected int pending;

		public FibActor(long n, CompletableFuture<Long> future) {
			this.n = n;
			this.future = future;
		}

		protected FibActor child(long n) {
			return new FibActor(n, null);
		}

		protected void result(long value) {
			if (future!=null)
				future.complete(value);
			else
				tell(value, RESULT, getParent());
		}

		@Override
		public void receive(ActorMessage<?> message) {
			if (message.tag()==CREATE) {
				if (n<2)
					result(n);
				else {
					pending = 2;
					tell(null, CREATE, addChild(() -> child(n-1)));
					tell(null, CREATE, addChild(() -> child(n-2)));
				}
			}
			else if (message.tag()==RESULT) {
				sum += (Long)message.value();
				if (--pending==0)
					result(sum);
			}
		}
	}

	protected static class PooledFibActor extends FibActor implements PooledActor {
		public PooledFibActor(long n, CompletableFuture<Long> future) {
			super(n, future);
		}

		@Override
		protected FibActor child(long n) {
			return new PooledFibActor(n, null);
		}
	}

	protected static long fib(ActorSystem system, String runtime, long n) throws Exception {
		CompletableFuture<Long> future = new CompletableFuture<>();
		ActorId root = system.addActor(() -> "hybrid".equals(runtime) ? new PooledFibActor(n, future) : new FibActor(n, future));
		system.send(ActorMessage.create(null, CREATE, system.SYSTEM_ID(), root));
		return future.get(60, TimeUnit.SECONDS);
	}

	protected record Result(double value, String text) {}

	protected static Result ring(String runtime, int threads) throws Exception {
		ActorSystem system = create(runtime, threads);
		int hops = 2_000;
		CountDownLatch done = new CountDownLatch(threads);
		LongAdder rounds = new LongAdder();
		List<ActorId> senders = rings(system, threads, done, rounds, new boolean[1]);
		system.start();
		Thread.sleep(100); // CREATE messages
		long start = System.nanoTime();
		for (ActorId sender : senders)
			system.send(ActorMessage.create(hops, HOP, system.SYSTEM_ID(), sender));
		done.await();
		long nanos = System.nanoTime()-start;
		system.shutdown(true);
		double mps = rounds.sum()*(double)RING_SIZE/nanos*1_000;
		return new Result(mps, String.format(Locale.ROOT, "%8.2f M msgs/s", mps));
	}

	protected static Result fib(String runtime, int threads) throws Exception {
		ActorSystem system = create(runtime, threads);
		system.start();
		long start = System.nanoTime();
		long value = fib(system, runtime, 22);
		double ms = (System.nanoTime()-start)/1e6;
		system.shutdown(true);
		if (value!=17711)
			throw new IllegalStateException("fib(22) = "+value);
		return new Result(ms, String.format(Locale.ROOT, "%8.1f ms fib(22)", ms));
	}

	protected static Result mixed(String runtime, int threads) throws Exception {
		ActorSystem system = create(runtime, threads);
		CountDownLatch done = new CountDownLatch(threads);
		LongAdder rounds = new LongAdder();
		boolean[] stop = new boolean[1];
		List<ActorId> senders = rings(system, threads, done, rounds, stop);
		system.start();
		Thread.sleep(100);
		for (ActorId sender : senders)
			system.send(ActorMessage.create(Integer.MAX_VALUE, HOP, system.SYSTEM_ID(), sender));
		Thread.sleep(200); // rings at full speed

		long[] latencies = new long[20];
		long roundsStart = rounds.sum();
		long start = System.nanoTime();
		for (int i=0; i<latencies.length; i++) {
			long t = System.nanoTime();
			fib(system, runtime, 18);
			latencies[i] = System.nanoTime()-t;
		}
		long nanos = System.nanoTime()-start;
		double mps = (rounds.sum()-roundsStart)*(double)RING_SIZE/nanos*1_000;
		stop[0] = true;
		done.await(10, TimeUnit.SECONDS);
		system.shutdown(true);

		Arrays.sort(latencies);
		double p50 = latencies[latencies.length/2]/1e6;
		return new Result(p50, String.format(Locale.ROOT, "fib(18) p50 %7.2f ms  max %7.2f ms   ring %6.2f M msgs/s meanwhile",
			p50, latencies[latencies.length-1]/1e6, mps));
	}

	// scatter: devices keep a running mean of their telemetry; telemetry tokens circulate within a group of devices
	// (closed loop, constant load); a query coordinator asks random devices and aggregates their answers
	protected static class Device extends ActorWithGroup {
		protected ActorId next;
		protected final LongAdder telemetry;
		protected final boolean[] stop;
		protected double mean;
		protected long count;

		public Device(ActorGroupSet group, LongAdder telemetry, boolean[] stop) {
			super(group);
			this.telemetry = telemetry;
			this.stop = stop;
		}

		@Override
		public void receive(ActorMessage<?> message) {
			if (message.tag()==TELEMETRY) {
				double value = (Double)message.value();
				count++;
				mean += (value-mean)/count;
				telemetry.increment();
				if (!stop[0])
					tell(value+1, TELEMETRY, next);
			}
			else if (message.tag()==QUERY)
				tell(mean, ANSWER, message.source());
			else if (message.tag()==CREATE)
				next = (ActorId)message.value();
		}
	}

	protected static class Coordinator extends Actor {
		protected final ActorId[] devices;
		protected final int fanOut;
		protected final CompletableFuture<Double> future;
		protected double sum;
		protected int pending;

		public Coordinator(ActorId[] devices, int fanOut, CompletableFuture<Double> future) {
			this.devices = devices;
			this.fanOut = fanOut;
			this.future = future;
		}

		@Override
		public void receive(ActorMessage<?> message) {
			if (message.tag()==START) {
				pending = fanOut;
				for (int i=0; i<fanOut; i++)
					tell(null, QUERY, devices[ThreadLocalRandom.current().nextInt(devices.length)]);
			}
			else if (message.tag()==ANSWER) {
				sum += (Double)message.value();
				if (--pending==0) {
					future.complete(sum/fanOut);
					stop();
				}
			}
		}
	}

	protected static class PooledCoordinator extends Coordinator implements PooledActor {
		public PooledCoordinator(ActorId[] devices, int fanOut, CompletableFuture<Double> future) {
			super(devices, fanOut, future);
		}
	}

	protected static Result scatter(String runtime, int threads) throws Exception {
		int devicesTotal = 2_000, queries = 1_000, concurrent = 50, fanOut = 200, tokensPerGroup = 16;
		ActorSystem system = create(runtime, threads);
		LongAdder telemetry = new LongAdder();
		boolean[] stop = new boolean[1];

		int groups = threads, perGroup = devicesTotal/groups;
		ActorId[] devices = new ActorId[groups*perGroup];
		for (int g=0; g<groups; g++) {
			ActorGroupSet group = new ActorGroupSet();
			for (int i=0; i<perGroup; i++)
				devices[g*perGroup+i] = system.addActor(() -> new Device(group, telemetry, stop));
			for (int i=0; i<perGroup; i++) // ring within the group
				system.send(ActorMessage.create(devices[g*perGroup+(i+1)%perGroup], CREATE, system.SYSTEM_ID(), devices[g*perGroup+i]));
		}
		system.start();
		Thread.sleep(100);
		for (int g=0; g<groups; g++)
			for (int t=0; t<tokensPerGroup; t++)
				system.send(ActorMessage.create(0.0, TELEMETRY, system.SYSTEM_ID(), devices[g*perGroup+t*(perGroup/tokensPerGroup)]));
		Thread.sleep(200); // telemetry at full speed

		long[] latencies = new long[queries];
		Semaphore inFlight = new Semaphore(concurrent);
		CountDownLatch done = new CountDownLatch(queries);
		long telemetryStart = telemetry.sum();
		long start = System.nanoTime();
		for (int q=0; q<queries; q++) {
			inFlight.acquire();
			final int index = q;
			final long t = System.nanoTime();
			CompletableFuture<Double> future = new CompletableFuture<>();
			future.whenComplete((v, e) -> {
				latencies[index] = System.nanoTime()-t;
				inFlight.release();
				done.countDown();
			});
			ActorId coordinator = system.addActor(() -> "hybrid".equals(runtime) ? new PooledCoordinator(devices, fanOut, future) : new Coordinator(devices, fanOut, future));
			system.send(ActorMessage.create(null, START, system.SYSTEM_ID(), coordinator));
		}
		done.await(120, TimeUnit.SECONDS);
		long nanos = System.nanoTime()-start;
		double mps = (telemetry.sum()-telemetryStart)/(double)nanos*1_000;
		stop[0] = true;
		system.shutdown(true);

		Arrays.sort(latencies);
		double p50 = latencies[queries/2]/1e6, p99 = latencies[(int)(queries*0.99)]/1e6;
		return new Result(p50, String.format(Locale.ROOT, "query p50 %7.2f ms  p99 %7.2f ms   telemetry %6.2f M msgs/s meanwhile   %5.0f queries/s",
			p50, p99, mps, queries/(nanos/1e9)));
	}

	protected static void single(String[] args) throws Exception {
		systemLogger().setLevel(ERROR);
		logger().setLevel(ERROR);
		String runtime = args[0], workload = args[1];
		int threads = Integer.parseInt(args[2]);
		if (args.length>3)
			pooled = Integer.parseInt(args[3]);

		List<Result> results = new ArrayList<>();
		for (int i=0; i<WARMUP+ITERATIONS; i++) {
			Result result = switch (workload) {
				case "ring" -> ring(runtime, threads);
				case "fib" -> fib(runtime, threads);
				case "mixed" -> mixed(runtime, threads);
				case "scatter" -> scatter(runtime, threads);
				default -> throw new IllegalArgumentException(workload);
			};
			if (i>=WARMUP)
				results.add(result);
		}
		results.sort((a, b) -> Double.compare(a.value(), b.value()));
		System.out.printf(Locale.ROOT, "%-8s %-6s threads=%-2d%s %s  [range %.2f..%.2f]%n", runtime, workload, threads, pooled>0 ? " pooled="+pooled : "",
			results.get(results.size()/2).text(), results.get(0).value(), results.get(results.size()-1).value());
	}

	protected static void matrix(String... args) throws Exception {
		int cores = Runtime.getRuntime().availableProcessors();
		String[] runtimes = {"default", "classic", "hybrid"};
		String[] workloads = args.length>0 ? args : new String[] {"ring", "fib", "mixed", "scatter"};
		int[] threadCounts = cores>8 ? new int[] {8, cores} : new int[] {cores};

		String java = System.getProperty("java.home")+File.separator+"bin"+File.separator+"java";
		String classpath = System.getProperty("java.class.path");
		System.out.printf("cores=%d, median of %d iterations (after %d warmup); hybrid: half of the threads pooled%n", cores, ITERATIONS, WARMUP);
		for (String workload : workloads)
			for (int threads : threadCounts) {
				for (String runtime : runtimes) {
					List<String> command = List.of(java, "-Xmx4g", "-cp", classpath, HybridBenchmark.class.getName(), runtime, workload, String.valueOf(threads));
					Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
					process.getInputStream().transferTo(System.out);
					process.waitFor();
				}
				System.out.println();
			}
	}

	public static void main(String[] args) throws Exception {
		if (args.length==0)
			matrix();
		else if (args[0].equals("matrix")) // matrix <workload...>
			matrix(Arrays.copyOfRange(args, 1, args.length));
		else
			single(args);
	}
}
