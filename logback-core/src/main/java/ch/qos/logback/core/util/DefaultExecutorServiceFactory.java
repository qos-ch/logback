/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.core.CoreConstants;

/**
 * Default instance used by {@link ExecutorServiceUtil} if {@link ExecutorServiceUtil#setFactory(ExecutorServiceFactory)} is not called.
 */
public class DefaultExecutorServiceFactory implements ExecutorServiceFactory {

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public Thread newThread(final Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }
            thread.setName("logback-" + threadNumber.getAndIncrement());
            return thread;
        }
    };

    @Override
    public ScheduledExecutorService newScheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(CoreConstants.SCHEDULED_EXECUTOR_POOL_SIZE, THREAD_FACTORY);
    }

    @Override
    public ExecutorService newExecutorService() {
        return new ThreadPoolExecutor(CoreConstants.CORE_POOL_SIZE, CoreConstants.MAX_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), THREAD_FACTORY);
    }

}
