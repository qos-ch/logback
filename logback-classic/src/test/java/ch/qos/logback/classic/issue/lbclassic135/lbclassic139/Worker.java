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

import java.util.Vector;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

/**
 * 
 * @author Olivier Cailloux
 * 
 */
public class Worker extends RunnableWithCounterAndDone {
    static final int SLEEP_DUIRATION = 50;

    private Logger logger;
    private final Vector lock = new Vector();

    final LoggerContext loggerContext;

    Worker(LoggerContext lc) {
        loggerContext = lc;
        logger = lc.getLogger(this.getClass());
    }

    public void run() {
        print("entered run()");
        while (!isDone()) {
            synchronized (lock) {
                sleep();
                logger.info("lock the logger");
            }
        }
        print("leaving run()");
    }

    @Override
    public String toString() {
        print("In Worker.toString() - about to access lock");
        synchronized (lock) {
            print("In Worker.toString() - got the lock");
            // sleep();
            return "STATUS";
        }
    }

    public void sleep() {
        try {
            print("About to go to sleep");
            Thread.sleep(SLEEP_DUIRATION);
            print("just woke up");
        } catch (InterruptedException exc) {
            exc.printStackTrace();
        }
    }

    void print(String msg) {
        String thread = Thread.currentThread().getName();
        System.out.println("[" + thread + "] " + msg);
    }
}