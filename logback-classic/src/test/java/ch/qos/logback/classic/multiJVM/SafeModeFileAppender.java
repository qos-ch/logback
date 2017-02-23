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

/**
 * An application to write to a file using a FileAppender in safe mode.
 * 
 * @author Ceki Gulcu
 * 
 */
public class SafeModeFileAppender {

    static long LEN;
    static String FILENAME;
    static String STAMP;

    static public void main(String[] argv) throws Exception {
        if (argv.length != 3) {
            usage("Wrong number of arguments.");
        }

        STAMP = argv[0];
        LEN = Integer.parseInt(argv[1]);
        FILENAME = argv[2];
        writeContinously(STAMP, FILENAME, true);
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SafeModeFileAppender.class.getName() + " stamp runLength filename\n" + " stamp JVM instance stamp\n"
                        + "   runLength (integer) the number of logs to generate perthread" + "    filename (string) the filename where to write\n");
        System.exit(1);
    }

    static LoggerContext buildLoggerContext(String stamp, String filename, boolean safetyMode) {
        LoggerContext loggerContext = new LoggerContext();

        FileAppender<ILoggingEvent> fa = new FileAppender<ILoggingEvent>();

        PatternLayoutEncoder patternLayout = new PatternLayoutEncoder();
        patternLayout.setPattern(stamp + " %5p - %m%n");
        patternLayout.setContext(loggerContext);
        patternLayout.start();

        fa.setEncoder(patternLayout);
        fa.setFile(filename);
        fa.setAppend(true);
        fa.setPrudent(safetyMode);
        fa.setContext(loggerContext);
        fa.start();

        ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fa);

        return loggerContext;
    }

    static void writeContinously(String stamp, String filename, boolean safetyMode) throws Exception {
        LoggerContext lc = buildLoggerContext(stamp, filename, safetyMode);
        Logger logger = lc.getLogger(SafeModeFileAppender.class);

        long before = System.nanoTime();
        for (int i = 0; i < LEN; i++) {
            logger.debug(LoggingThread.msgLong + " " + i);
        }
        lc.stop();
        double durationPerLog = (System.nanoTime() - before) / (LEN * 1000.0);

        System.out.println("Average duration of " + (durationPerLog) + " microseconds per log. Safety mode " + safetyMode);
        System.out.println("------------------------------------------------");
    }
}
