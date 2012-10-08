package org.elasticsearch.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.common.unit.TimeValue;

public interface ThreadPool {

    interface Names {
        String SAME = "same";
        String GENERIC = "generic";
        String GET = "get";
        String INDEX = "index";
        String BULK = "bulk";
        String SEARCH = "search";
        String PERCOLATE = "percolate";
        String MANAGEMENT = "management";
        String FLUSH = "flush";
        String MERGE = "merge";
        String CACHE = "cache";
        String REFRESH = "refresh";
        String SNAPSHOT = "snapshot";
    }    
    
    ThreadPoolInfo info();
    
    ThreadPoolStats stats();
    
    Executor generic();
    
    long estimatedTimeInMillis();
    
    Executor executor(String name);
    
    ScheduledExecutorService scheduler();
    
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, TimeValue interval);
    
    ScheduledFuture<?> schedule(TimeValue delay, String name, Runnable command);
    
    void shutdown();
    
    void shutdownNow();
    
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
