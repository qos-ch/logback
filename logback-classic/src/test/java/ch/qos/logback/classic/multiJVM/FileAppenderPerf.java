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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.testUtil.RandomUtil;

public class FileAppenderPerf {
    static String msgLong = "ABCDEGHIJKLMNOPQRSTUVWXYZabcdeghijklmnopqrstuvwxyz1234567890";

    static long LEN = 100 * 1000;
    static int DIFF = RandomUtil.getPositiveInt() % 1000;
    static String FILENAME;

    static LoggerContext buildLoggerContext(final String filename, final boolean safetyMode) {
        final LoggerContext loggerContext = new LoggerContext();

        final FileAppender<ILoggingEvent> fa = new FileAppender<>();

        final PatternLayoutEncoder patternLayout = new PatternLayoutEncoder();
        patternLayout.setPattern("%5p %c - %m%n");
        patternLayout.setContext(loggerContext);
        patternLayout.start();

        fa.setEncoder(patternLayout);
        fa.setFile(filename);
        fa.setAppend(false);
        fa.setPrudent(safetyMode);
        fa.setContext(loggerContext);
        fa.start();

        final ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fa);

        return loggerContext;
    }

    static void usage(final String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + FileAppenderPerf.class.getName() + " filename");

        System.exit(1);
    }

    public static void main(final String[] argv) throws Exception {
        if (argv.length > 1) {
            usage("Wrong number of arguments.");
        }

        if (argv.length == 0) {
            FILENAME = DIFF + "";
        } else {
            FILENAME = argv[0];
        }

        perfCase(false);
        perfCase(true);
    }

    static void perfCase(final boolean safetyMode) throws Exception {
        final LoggerContext lc = buildLoggerContext(FILENAME + "-" + safetyMode + ".log", safetyMode);
        final Logger logger = lc.getLogger(FileAppenderPerf.class);

        final long start = System.nanoTime();
        for (int i = 0; i < LEN; i++) {
            logger.debug(msgLong + " " + i);
        }
        // in microseconds
        final double durationPerLog = (System.nanoTime() - start) / (LEN * 1000.0);

        lc.stop();

        System.out.println("Average duration of " + durationPerLog + " microseconds per log. Prudent mode=" + safetyMode);
        System.out.println("------------------------------------------------");
    }

}
