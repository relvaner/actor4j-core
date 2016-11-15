Actor4j an actor implementation
===============================
Aim of this project was to enhance the performance in message passing. As a reference implementation Akka [1] was used. Results of the research shown that intra-thread-communication is much better than inter-thread-communication. You can group actors, so they are bound to the same thread, for instance. Message queues of the actors are outsourced to the thread. The four principles of reactive manifesto [2] and the four semantic properties [3] of actor systems have been applied. The actor system is from extern accessible by the REST-Api or by a websocket. Between the nodes are websockets for message transfer established. Time consuming tasks can be outsourced to ResourceActors, which are executed by an extra ThreadPool. So the responsiveness of the actor system therfore will not tangented.

[1] Lightbend (2016). Akka. http://akka.io/
[2] Jonas Bonér, Dave Farley, Roland Kuhn, and Martin Thompson (2014). The Reactive Manifesto. http://www.reactivemanifesto.org/
[3] Rajesh K. Karmani, Gul Agha (2011). Actors. In Encyclopedia of Parallel Computing, Pages 1–11. Springer. http://osl.cs.illinois.edu/media/papers/karmani-2011-actors.pdf

Configuration, starting and stopping the actor system
=====================================================
In actor4j the following important configuration options are available.
<pre><code>
ActorSystem system = new ActorSystem();

system
	.setParallelismMin(1)
	.setParallelismFactor(1);
	.softMode(); // or .hardMode();
</code></pre>
On the one hand, the number of threads can be set with setParallelismMin and the scaling factor with setParallelismFactor:
<pre><code>
Number of threads = parallelismMin * parallelismFactor
</code></pre>
In addition, it can be determined whether the threads are operating in soft or hard mode if the situation occurs that temporarily no messages are received. The actor system is started with the call:
<pre><code>
system.start();
</code></pre>
The actor system can be terminated, either with controlled shutdown of all actuators or not. With a controlled shutdown, a stop directive is sent internally to all actuators. By means of parameter transfer, it is possible to determine whether the calling thread waits until the shutdown of the actor system has been completely terminated.
<pre><code>
system.shutdown(); // normales Herunterfahren
system.shutdown(true); // Herunterfahren und Warten

system.shutdownWithActors(); // Herunterfahren mit Aktoren
system.shutdownWithActors(true);
</code></pre>

<b>Page to be updated 11/15/2016<b>