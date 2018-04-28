package actor4j.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// see Executers.java -> DefaultThreadFactory
public class ActorThreadFactory implements ThreadFactory {
    protected final ThreadGroup group;
    protected final String name;
    protected final AtomicInteger index = new AtomicInteger(0);

    public ActorThreadFactory(String name) {
    	this.name = name;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              name + "-" + index.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
