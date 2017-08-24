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
package ch.qos.logback.classic.issue.lbclassic135;

import org.slf4j.Logger;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;

public class LoggingRunnable extends RunnableWithCounterAndDone {

    final Logger logger;
    final int burstLength;

    public LoggingRunnable(Logger logger, int burstLength) {
        this.logger = logger;
        this.burstLength = burstLength;
    }

    public LoggingRunnable(Logger logger) {
        this(logger, 10);
    }

    public void run() {
        while (!isDone()) {
            logger.info("hello world ABCDEFGHI");
            counter++;
            // don't hog the CPU forever
            if (counter % burstLength == 0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
