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

package org.elasticsearch.common.util.concurrent;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.jsr166y.LinkedTransferQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ClientEsExecutors {

    public static ThreadPoolExecutor newScalingExecutorService(int min, int max, long keepAliveTime, TimeUnit unit,
                                                                 ThreadFactory threadFactory) {
        ExecutorScalingQueue<Runnable> queue = new ExecutorScalingQueue<Runnable>();
        // we force the execution, since we might run into concurrency issues in offer for ScalingBlockingQueue
        ThreadPoolExecutor executor = new ThreadPoolExecutor(min, max, keepAliveTime, unit, queue, threadFactory,
                new ForceQueuePolicy());
        queue.executor = executor;
        return executor;
    }

    public static String threadName(Settings settings, String namePrefix) {
        String name = settings.get("name");
        if (name == null) {
            name = "elasticsearch";
        } else {
            name = "elasticsearch[" + name + "]";
        }
        return name + "[" + namePrefix + "]";
    }

    public static ThreadFactory daemonThreadFactory(Settings settings, String namePrefix) {
        return daemonThreadFactory(threadName(settings, namePrefix));
    }

    public static ThreadFactory daemonThreadFactory(String namePrefix) {
        return new EsThreadFactory(namePrefix);
    }

    static class EsThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        public EsThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + "[T#" + threadNumber.getAndIncrement() + "]",
                    0);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Cannot instantiate.
     */
    private ClientEsExecutors() {
    }

    static class ExecutorScalingQueue<E> extends LinkedTransferQueue<E> {

        ThreadPoolExecutor executor;

        public ExecutorScalingQueue() {
        }

        @Override
        public boolean offer(E e) {
            int left = executor.getMaximumPoolSize() - executor.getCorePoolSize();
            if (!tryTransfer(e)) {
                if (left > 0) {
                    return false;
                } else {
                    return super.offer(e);
                }
            } else {
                return true;
            }
        }
    }

    static class ExecutorBlockingQueue<E> extends ArrayBlockingQueue<E> {

        ThreadPoolExecutor executor;

        ExecutorBlockingQueue(int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(E o) {
            int allWorkingThreads = executor.getActiveCount() + super.size();
            return allWorkingThreads < executor.getPoolSize() && super.offer(o);
        }
    }


    /**
     * A handler for rejected tasks that adds the specified element to this queue,
     * waiting if necessary for space to become available.
     */
    static class ForceQueuePolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                //should never happen since we never wait
                throw new EsRejectedExecutionException(e);
            }
        }
    }

}
