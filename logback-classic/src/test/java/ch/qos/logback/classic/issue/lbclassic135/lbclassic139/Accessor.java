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

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * 
 * @author Olivier Cailloux
 * 
 */
public class Accessor extends RunnableWithCounterAndDone {
    private Logger logger;
    final Worker worker;
    final LoggerContext loggerContext;

    Accessor(Worker worker, LoggerContext lc) {
        this.worker = worker;
        this.loggerContext = lc;
        logger = lc.getLogger(this.getClass());
    }

    public void run() {
        print("entered run()");
        // Thread.yield();
        while (!isDone()) {
            logger.info("Current worker status is: {}.", worker);
        }
        print("leaving run()");
    }

    void print(String msg) {
        String thread = Thread.currentThread().getName();
        System.out.println("[" + thread + "] " + msg);
    }
}
