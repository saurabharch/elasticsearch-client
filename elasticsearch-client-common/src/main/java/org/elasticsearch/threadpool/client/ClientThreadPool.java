/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.threadpool.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.threadpool.ThreadPoolInfo;
import org.elasticsearch.threadpool.ThreadPoolInfoElement;
import org.elasticsearch.threadpool.ThreadPoolStats;
import org.elasticsearch.threadpool.ThreadPoolStatsElement;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.unit.TimeValue.timeValueMinutes;

/**
 *
 */
public class ClientThreadPool implements ThreadPool {

    private static ESLogger logger = ESLoggerFactory.getLogger(ClientThreadPool.class.getName());

    private final Settings settings;

    public static class Names {
        public static final String SAME = "same";
        public static final String GENERIC = "generic";
    }

    private final ImmutableMap<String, ExecutorHolder> executors;

    private final ScheduledThreadPoolExecutor scheduler;

    private final EstimatedTimeThread estimatedTimeThread;

    public ClientThreadPool() {
        this(ImmutableSettings.Builder.EMPTY_SETTINGS);
    }

    public ClientThreadPool(Settings settings) {
        this.settings = settings;
        
        Map<String, Settings> groupSettings = settings.getGroups("threadpool");

        Map<String, ExecutorHolder> executors = Maps.newHashMap();
        executors.put(Names.GENERIC, build(Names.GENERIC, "cached", groupSettings.get(Names.GENERIC), settingsBuilder().put("keep_alive", "30s").build()));
        executors.put(Names.SAME, new ExecutorHolder(MoreExecutors.sameThreadExecutor(), new ClientThreadPoolInfoElement(Names.SAME, "same")));
        this.executors = ImmutableMap.copyOf(executors);
        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, ClientEsExecutors.daemonThreadFactory(settings, "scheduler"));
        this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        //TimeValue estimatedTimeInterval = componentSettings.getAsTime("estimated_time_interval", TimeValue.timeValueMillis(200));
        TimeValue estimatedTimeInterval = TimeValue.timeValueMillis(200);
        this.estimatedTimeThread = new EstimatedTimeThread(ClientEsExecutors.threadName(settings, "[timer]"), estimatedTimeInterval.millis());
        this.estimatedTimeThread.start();
    }

    public long estimatedTimeInMillis() {
        return estimatedTimeThread.estimatedTimeInMillis();
    }

    public ThreadPoolInfo info() {
        List<ThreadPoolInfoElement> infos = new ArrayList();
        for (ExecutorHolder holder : executors.values()) {
            String name = holder.info.name();
            // no need to have info on "same" thread pool
            if ("same".equals(name)) {
                continue;
            }
            infos.add(holder.info);
        }
        return new ClientThreadPoolInfo(infos);
    }

    public ThreadPoolStats stats() {
        List<ThreadPoolStatsElement> stats = new ArrayList();
        for (ExecutorHolder holder : executors.values()) {
            String name = holder.info.name();
            // no need to have info on "same" thread pool
            if ("same".equals(name)) {
                continue;
            }
            int threads = -1;
            int queue = -1;
            int active = -1;
            long rejected = -1;
            if (holder.executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) holder.executor;
                threads = threadPoolExecutor.getPoolSize();
                queue = threadPoolExecutor.getQueue().size();
                active = threadPoolExecutor.getActiveCount();
                RejectedExecutionHandler rejectedExecutionHandler = threadPoolExecutor.getRejectedExecutionHandler();
                if (rejectedExecutionHandler instanceof XRejectedExecutionHandler) {
                    rejected = ((XRejectedExecutionHandler) rejectedExecutionHandler).rejected();
                }
            }
            stats.add(new ClientThreadPoolStatsElement(name, threads, queue, active, rejected));
        }
        return new ClientThreadPoolStats(stats);
    }

    public Executor generic() {
        return executor(Names.GENERIC);
    }

    public Executor executor(String name) {
        Executor executor = executors.get(name).executor;
        if (executor == null) {
            throw new ElasticSearchIllegalArgumentException("No executor found for [" + name + "]");
        }
        return executor;
    }

    public ScheduledExecutorService scheduler() {
        return this.scheduler;
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, TimeValue interval) {
        return scheduler.scheduleWithFixedDelay(new LoggingRunnable(command), interval.millis(), interval.millis(), TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(TimeValue delay, String name, Runnable command) {
        if (!Names.SAME.equals(name)) {
            command = new ThreadedRunnable(command, executor(name));
        }
        return scheduler.schedule(command, delay.millis(), TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        estimatedTimeThread.running = false;
        estimatedTimeThread.interrupt();
        scheduler.shutdown();
        for (ExecutorHolder executor : executors.values()) {
            if (executor.executor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor) executor.executor).shutdown();
            }
        }
    }

    public void shutdownNow() {
        estimatedTimeThread.running = false;
        estimatedTimeThread.interrupt();
        scheduler.shutdownNow();
        for (ExecutorHolder executor : executors.values()) {
            if (executor.executor instanceof ThreadPoolExecutor) {
                ((ThreadPoolExecutor) executor.executor).shutdownNow();
            }
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean result = scheduler.awaitTermination(timeout, unit);
        for (ExecutorHolder executor : executors.values()) {
            if (executor.executor instanceof ThreadPoolExecutor) {
                result &= ((ThreadPoolExecutor) executor.executor).awaitTermination(timeout, unit);
            }
        }
        return result;
    }

    private ExecutorHolder build(String name, String defaultType, @Nullable Settings settings, Settings defaultSettings) {
        if (settings == null) {
            settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
        }
        String type = settings.get("type", defaultType);
        ThreadFactory threadFactory = ClientEsExecutors.daemonThreadFactory(this.settings, name);
        if ("same".equals(type)) {
            logger.debug("creating thread_pool [{}], type [{}]", name, type);
            return new ExecutorHolder(MoreExecutors.sameThreadExecutor(), new ClientThreadPoolInfoElement(name, type));
        } else if ("cached".equals(type)) {
            TimeValue keepAlive = settings.getAsTime("keep_alive", defaultSettings.getAsTime("keep_alive", timeValueMinutes(5)));
            logger.debug("creating thread_pool [{}], type [{}], keep_alive [{}]", name, type, keepAlive);
            Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    keepAlive.millis(), TimeUnit.MILLISECONDS,
                    new SynchronousQueue<Runnable>(),
                    threadFactory);
            return new ExecutorHolder(executor, new ClientThreadPoolInfoElement(name, type, -1, -1, keepAlive, null));
        }
        throw new ElasticSearchIllegalArgumentException("No type found [" + type + "], for [" + name + "]");
    }

    class LoggingRunnable implements Runnable {

        private final Runnable runnable;

        LoggingRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warn("failed to run {}", e, runnable.toString());
            }
        }

        @Override
        public int hashCode() {
            return runnable.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return runnable.equals(obj);
        }

        @Override
        public String toString() {
            return "[threaded] " + runnable.toString();
        }
    }

    class ThreadedRunnable implements Runnable {

        private final Runnable runnable;

        private final Executor executor;

        ThreadedRunnable(Runnable runnable, Executor executor) {
            this.runnable = runnable;
            this.executor = executor;
        }

        @Override
        public void run() {
            executor.execute(runnable);
        }

        @Override
        public int hashCode() {
            return runnable.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return runnable.equals(obj);
        }

        @Override
        public String toString() {
            return "[threaded] " + runnable.toString();
        }
    }

    static class EstimatedTimeThread extends Thread {

        final long interval;

        volatile boolean running = true;

        volatile long estimatedTimeInMillis;

        EstimatedTimeThread(String name, long interval) {
            super(name);
            this.interval = interval;
            setDaemon(true);
        }

        public long estimatedTimeInMillis() {
            return this.estimatedTimeInMillis;
        }

        @Override
        public void run() {
            while (running) {
                estimatedTimeInMillis = System.currentTimeMillis();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    running = false;
                    return;
                }
                try {
                    FileSystemUtils.checkMkdirsStall(estimatedTimeInMillis);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    static class ExecutorHolder {
        public final Executor executor;
        public final ThreadPoolInfoElement info;

        ExecutorHolder(Executor executor, ThreadPoolInfoElement info) {
            this.executor = executor;
            this.info = info;
        }
    }

}
