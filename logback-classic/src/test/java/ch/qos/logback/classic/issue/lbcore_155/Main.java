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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
        StatusPrinter.print((LoggerContext) LoggerFactory.getILoggerFactory());
        OThread ot = new OThread();
        ot.start();
        Thread.sleep(OThread.WAIT_MILLIS - 500);
        logger.info("About to interrupt");
        ot.interrupt();
        logger.info("After interrupt");
        logger.info("Leaving main");

    }
}
