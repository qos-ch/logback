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
import java.util.concurrent.ScheduledExecutorService;

/**
 * Static utility methods for manipulating an {@link ExecutorService}.
 * @author Carl Harris
 * @author Mikhail Mazursky
 */
public class ExecutorServiceUtil {

    private static Object mutex = new Object();

    private static ExecutorServiceFactory factory = null;

    private static ExecutorServiceFactory volatileFactory = null;

    private static ExecutorServiceFactory getFactory() {
        if (factory == null) {
            if (volatileFactory == null) {
                synchronized (mutex) {
                    if (volatileFactory == null) {
                        volatileFactory = new DefaultExecutorServiceFactory();
                    }
                }
            }
            factory = volatileFactory;
        }
        return factory;
    }

    public static void setFactory(final ExecutorServiceFactory executorServiceFactory) {
        if (factory == null) {
            if (volatileFactory == null) {
                synchronized (mutex) {
                    if (volatileFactory == null) {
                        volatileFactory = executorServiceFactory;
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("Factory already initialized");
    }

    static public ScheduledExecutorService newScheduledExecutorService() {
        return getFactory().newScheduledExecutorService();
    }

    /**
     * Creates an executor service suitable for use by logback components.
     * @return executor service
     */
    static public ExecutorService newExecutorService() {
        return getFactory().newExecutorService();
    }

    /**
     * Shuts down an executor service.
     * <p>
     * @param executorService the executor service to shut down
     */
    static public void shutdown(final ExecutorService executorService) {
        executorService.shutdownNow();
    }

}
