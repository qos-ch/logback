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
package ch.qos.logback.classic.issue.lbcore_155;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class OThread extends Thread {

    static int NANOS_IN_MILLI = 1000 * 1000;

    static int WAIT_MILLIS = 10;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void run() {

        while (!isInterrupted()) {
            long start = System.nanoTime();
            for (long now = System.nanoTime(); now < start + 2 * WAIT_MILLIS * NANOS_IN_MILLI; now = System.nanoTime()) {
                logger.info("in time loop");
            }

            logger.info("before 2nd sleep");

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                logger.info("While sleeping", e);
                e.printStackTrace();
                break;
            }
            logger.info("after sleep");
        }
        logger.info("exiting WHILE");

    }
}
