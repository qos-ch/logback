/**
 * Logback: the reliable, generic, fast and flexible logging framework. Copyright (C) 1999-2015, QOS.ch. All rights
 * reserved.
 *
 * This program and the accompanying materials are dual-licensed under either the terms of the Eclipse Public License
 * v1.0 as published by the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package ch.qos.logback.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.core.CoreConstants;

/**
 * Static utility methods for manipulating an {@link ExecutorService}.
 *
 * @author Carl Harris
 * @author Mikhail Mazursky
 */
public class ExecutorServiceUtil {

    private static final ThreadFactory THREAD_FACTORY_FOR_SCHEDULED_EXECUTION_SERVICE = new ThreadFactory() {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final ThreadFactory defaultFactory = makeThreadFactory();

        /**
         * A thread factory which may be a virtual thread factory the JDK supports it.
         *
         * @return
         */
        private ThreadFactory makeThreadFactory() {
            return Executors.defaultThreadFactory();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            if (!thread.isDaemon()) {
                thread.setDaemon(true);
            }
            thread.setName("logback-" + threadNumber.getAndIncrement());
            return thread;
        }
    };

    static public ScheduledExecutorService newScheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(CoreConstants.SCHEDULED_EXECUTOR_POOL_SIZE, THREAD_FACTORY_FOR_SCHEDULED_EXECUTION_SERVICE);
    }

    /**
     * @deprecated replaced by {@link #newThreadPoolExecutor()}
     */
    static public ExecutorService newExecutorService() {
        return newThreadPoolExecutor();
    }

    /**
     * Creates an ThreadPoolExecutor suitable for use by logback components.
     *
     * @since 1.4.7
     * @return ThreadPoolExecutor
     */
    static public ThreadPoolExecutor newThreadPoolExecutor() {

        // irrelevant parameter when LinkedBlockingQueue is in use
        final int maximumPoolSize = CoreConstants.CORE_POOL_SIZE + 1;
        final long keepAliveMillis = 100L;

        // As of version 1.5.13, the SynchronousQueue was replaced by LinkedBlockingQueue
        // This has the effect of queueing jobs immediately and have them run by CORE_POOL_SIZE
        // threads. We expect jobs to arrive at a relatively slow pace compared to their duration.
        // Note that threads are removed if idle more than keepAliveMillis
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(CoreConstants.CORE_POOL_SIZE, maximumPoolSize, keepAliveMillis, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(), THREAD_FACTORY_FOR_SCHEDULED_EXECUTION_SERVICE);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;

    }

    /**
     * Shuts down an executor service.
     * <p>
     *
     * @param executorService the executor service to shut down
     */
    static public void shutdown(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    /**
     * An alternate implementation of {@linl #newThreadPoolExecutor} which returns a virtual thread per task executor when
     * available.
     *
     * @since 1.3.12/1.4.12
     */
    static public ExecutorService newAlternateThreadPoolExecutor() {
        return newThreadPoolExecutor();
    }
}
