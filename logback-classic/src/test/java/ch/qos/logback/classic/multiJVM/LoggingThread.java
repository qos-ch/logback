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
package ch.qos.logback.classic.multiJVM;

import org.slf4j.Logger;

public class LoggingThread extends Thread {
    static String msgLong = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    final long len;
    final Logger logger;
    private double durationPerLog;

    public LoggingThread(Logger logger, long len) {
        this.logger = logger;
        this.len = len;
    }

    public void run() {
        long before = System.nanoTime();
        for (int i = 0; i < len; i++) {
            logger.debug(msgLong + " " + i);
            // try {
            // Thread.sleep(100);
            // } catch (InterruptedException e) {
            // }
        }
        // in microseconds
        durationPerLog = (System.nanoTime() - before) / (len * 1000.0);
    }

    public double getDurationPerLogInMicroseconds() {
        return durationPerLog;
    }

}
