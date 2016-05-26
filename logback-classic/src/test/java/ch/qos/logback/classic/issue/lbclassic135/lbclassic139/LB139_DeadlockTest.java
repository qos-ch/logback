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
package ch.qos.logback.classic.issue.lbclassic135.lbclassic139;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;

public class LB139_DeadlockTest {

    LoggerContext loggerContext = new LoggerContext();

    @Before
    public void setUp() {
        loggerContext.setName("LB139");
        BasicConfigurator bc = new BasicConfigurator();
        bc.setContext(loggerContext);
        bc.configure(loggerContext);
    }

    @Test
    // (timeout=3000)
    public void test() throws Exception {
        Worker worker = new Worker(loggerContext);
        Accessor accessor = new Accessor(worker, loggerContext);

        Thread workerThread = new Thread(worker, "WorkerThread");
        Thread accessorThread = new Thread(accessor, "AccessorThread");

        workerThread.start();
        accessorThread.start();

        int sleep = Worker.SLEEP_DUIRATION * 10;

        System.out.println("Will sleep for " + sleep + " millis");
        Thread.sleep(sleep);
        System.out.println("Done sleeping (" + sleep + " millis)");
        worker.setDone(true);
        accessor.setDone(true);

        workerThread.join();
        accessorThread.join();
    }
}
