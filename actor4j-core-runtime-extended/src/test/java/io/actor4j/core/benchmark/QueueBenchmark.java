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

import java.io.File;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.MpscLinkedQueue;

// AI-generated.
// Queue benchmark: N producers -> 1 consumer (JCTools vs. JDK).
// Not a unit test (no *Test name), run manually: without arguments the whole matrix is executed,
// each configuration in its own JVM (no JIT profile pollution by other queue types).
// Single run: <type> <producers> <capacity> <messages> <consumerWorkNs> <spin|drop>
//   spin: producer retries on full queue, drop: producer drops on full queue (like BoundedActorThread)
public class QueueBenchmark {
	protected static final Object MSG = new Object();

	protected static final int WARMUP = 2;
	protected static final int ITERATIONS = 5;

	// bounded ConcurrentLinkedQueue with a soft limit (shared counter)
	protected static final class CountedConcurrentLinkedQueue<E> extends AbstractQueue<E> {
		protected final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();
		protected final AtomicInteger size = new AtomicInteger();
		protected final int capacity;

		public CountedConcurrentLinkedQueue(int capacity) {
			this.capacity = capacity;
		}

		@Override
		public boolean offer(E e) {
			if (size.incrementAndGet()>capacity) {
				size.decrementAndGet();
				return false;
			}
			return queue.offer(e);
		}

		@Override
		public E poll() {
			E result = queue.poll();
			if (result!=null)
				size.decrementAndGet();
			return result;
		}

		@Override
		public E peek() {
			return queue.peek();
		}

		@Override
		public int size() {
			return size.get();
		}

		@Override
		public Iterator<E> iterator() {
			return queue.iterator();
		}
	}

	protected static Queue<Object> createQueue(String type, int capacity) {
		return switch (type) {
			case "MpscLinkedQueue"   -> new MpscLinkedQueue<>();
			case "ConcurrentLinkedQueue" -> new ConcurrentLinkedQueue<>();
			case "MpscArrayQueue"    -> new MpscArrayQueue<>(capacity);
			case "ArrayBlockingQueue" -> new ArrayBlockingQueue<>(capacity);
			case "CountedConcurrentLinkedQueue" -> new CountedConcurrentLinkedQueue<>(capacity);
			default -> throw new IllegalArgumentException(type);
		};
	}

	protected static void work(long ns) {
		if (ns>0) {
			long start = System.nanoTime();
			while (System.nanoTime()-start<ns)
				Thread.onSpinWait();
		}
	}

	// {offers/s of producers (Mops), delivered/s at consumer (Mops), drop rate}
	protected static double[] run(String type, int producers, int capacity, long messages, long workNs, boolean drop) throws Exception {
		Queue<Object> queue = createQueue(type, capacity);
		long perProducer = messages/producers;
		CyclicBarrier start = new CyclicBarrier(producers+1);
		AtomicLong dropped = new AtomicLong();
		AtomicInteger running = new AtomicInteger(producers);
		AtomicLong producerEnd = new AtomicLong();

		Thread[] threads = new Thread[producers];
		for (int p=0; p<producers; p++) {
			threads[p] = new Thread(() -> {
				long d = 0;
				try {
					start.await();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				for (long i=0; i<perProducer; i++)
					if (!queue.offer(MSG)) {
						if (drop)
							d++;
						else
							while (!queue.offer(MSG))
								Thread.onSpinWait();
					}
				dropped.addAndGet(d);
				if (running.decrementAndGet()==0)
					producerEnd.set(System.nanoTime());
			});
			threads[p].start();
		}

		start.await();
		long t0 = System.nanoTime();
		long received = 0;
		for (;;) {
			if (queue.poll()!=null) {
				received++;
				work(workNs);
			}
			else if (running.get()==0 && queue.isEmpty())
				break;
			else
				Thread.onSpinWait();
		}
		long t1 = System.nanoTime();
		for (Thread t : threads)
			t.join();

		long offers = perProducer*producers;
		return new double[] { offers/((producerEnd.get()-t0)/1e9)/1e6, received/((t1-t0)/1e9)/1e6, dropped.get()/(double)offers };
	}

	protected static void single(String[] args) throws Exception {
		String type = args[0];
		int producers = Integer.parseInt(args[1]);
		int capacity = Integer.parseInt(args[2]);
		long messages = Long.parseLong(args[3]);
		long workNs = Long.parseLong(args[4]);
		boolean drop = args[5].equals("drop");

		for (int i=0; i<WARMUP; i++)
			run(type, producers, capacity, messages, workNs, drop);
		double[][] results = new double[ITERATIONS][];
		for (int i=0; i<ITERATIONS; i++)
			results[i] = run(type, producers, capacity, messages, workNs, drop);
		Arrays.sort(results, Comparator.comparingDouble(r -> r[1]));
		double[] median = results[ITERATIONS/2];

		System.out.printf(Locale.ROOT, "%-28s producers=%-2d capacity=%-6s work=%4dns %-4s offers=%8.2f Mops/s  delivered=%6.2f Mops/s  dropped=%5.1f%%%n",
			type, producers, capacity>0 ? String.valueOf(capacity) : "-", workNs, args[5], median[0], median[1], median[2]*100);
	}

	protected static void matrix() throws Exception {
		List<String[]> configs = new ArrayList<>();

		String[] unbounded = { "MpscLinkedQueue", "ConcurrentLinkedQueue" };
		String[] bounded = { "MpscArrayQueue", "ArrayBlockingQueue", "CountedConcurrentLinkedQueue" };

		// (1) raw queue throughput, consumer without work
		for (int p : new int[] { 1, 4, 16 })
			for (String type : unbounded)
				configs.add(new String[] { type, ""+p, "0", "10000000", "0", "spin" });
		for (int capacity : new int[] { 50_000, 1024 })
			for (int p : new int[] { 1, 4, 16 })
				for (String type : bounded)
					configs.add(new String[] { type, ""+p, ""+capacity, "10000000", "0", "spin" });

		// (2) consumer with work per message
		for (long workNs : new long[] { 100, 1000 })
			for (int p : new int[] { 4, 16 })
				for (String type : unbounded)
					configs.add(new String[] { type, ""+p, "0", workNs==1000 ? "2000000" : "5000000", ""+workNs, "spin" });

		// (3) flood with drop (BoundedActorThread), consumer 1 µs per message
		for (int p : new int[] { 4, 16 })
			for (String type : bounded)
				configs.add(new String[] { type, ""+p, "50000", "20000000", "1000", "drop" });

		String java = System.getProperty("java.home")+File.separator+"bin"+File.separator+"java";
		String classpath = System.getProperty("java.class.path");
		for (String[] config : configs) {
			List<String> command = new ArrayList<>(List.of(java, "-Xmx4g", "-cp", classpath, QueueBenchmark.class.getName()));
			command.addAll(List.of(config));
			Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
			process.inputReader().lines()
				.filter(line -> !line.startsWith("WARNING"))
				.forEach(System.out::println);
			process.waitFor();
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length==0)
			matrix();
		else
			single(args);
	}
}
