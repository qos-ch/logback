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
package ch.qos.logback.classic.issue.lbcore243;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Results AMD Phenom II X6 1110T processor and SSD disk
//Logback  with    immediate flush: 8356 nanos per call
//Logback  without immediate flush: 1758 nanos per call

public class PerformanceComparatorLogback {
    static Logger logbacklogger = LoggerFactory.getLogger(PerformanceComparatorLogback.class);

    public static void main(String[] args) throws JoranException, InterruptedException {
        initLogbackWithoutImmediateFlush();
        logbackParametrizedDebugCall();

        initLogbackWithImmediateFlush();
        logbackParametrizedDebugCall();
        System.out.println("###############################################");
        System.out.println("Logback  with    immediate flush: " + logbackParametrizedDebugCall() + " nanos per call");

        initLogbackWithoutImmediateFlush();
        System.out.println("Logback  without immediate flush: " + logbackParametrizedDebugCall() + " nanos per call");

        System.out.println("###############################################");
    }

    private static long logbackParametrizedDebugCall() {

        Integer j = Integer.valueOf(2);
        long start = System.nanoTime();
        for (int i = 0; i < Common.loop; i++) {
            logbacklogger.debug("SEE IF THIS IS LOGGED {}.", j);
        }
        return (System.nanoTime() - start) / Common.loop;
    }

    static String DIR_PREFIX = "src/test/java/ch/qos/logback/classic/issue/lbcore243/";

    static void configure(String file) throws JoranException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.reset();
        jc.doConfigure(file);
    }

    private static void initLogbackWithoutImmediateFlush() throws JoranException {
        configure(DIR_PREFIX + "logback_without_immediateFlush.xml");
    }

    private static void initLogbackWithImmediateFlush() throws JoranException {
        configure(DIR_PREFIX + "logback_with_immediateFlush.xml");
    }
}