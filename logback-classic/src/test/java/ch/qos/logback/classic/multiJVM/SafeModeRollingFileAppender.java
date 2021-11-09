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
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * An application to write to a file using a RollingFileAppender in safe mode.
 *
 * @author Ceki Gulcu
 *
 */
public class SafeModeRollingFileAppender {

	static long LEN;
	static String FILENAME;
	static String STAMP;

	static final String DATE_PATTERN = "yyyy-MM-dd_HH_mm_ss";

	static public void main(final String[] argv) throws Exception {
		if (argv.length != 3) {
			usage("Wrong number of arguments.");
		}

		STAMP = argv[0];
		LEN = Integer.parseInt(argv[1]);
		FILENAME = argv[2];
		writeContinously(STAMP, FILENAME, true);
	}

	static void usage(final String msg) {
		System.err.println(msg);
		System.err.println("Usage: java " + SafeModeRollingFileAppender.class.getName() + " stamp runLength filename\n" + " stamp JVM instance stamp\n"
				+ "   runLength (integer) the number of logs to generate perthread" + "    filename (string) the filename where to write\n");
		System.exit(1);
	}

	static LoggerContext buildLoggerContext(final String stamp, final String filename, final boolean safetyMode) {
		final LoggerContext loggerContext = new LoggerContext();

		final RollingFileAppender<ILoggingEvent> rfa = new RollingFileAppender<>();
		final PatternLayoutEncoder patternLayout = new PatternLayoutEncoder();
		patternLayout.setPattern(stamp + " %5p - %-50m%n");
		patternLayout.setContext(loggerContext);
		patternLayout.start();

		rfa.setEncoder(patternLayout);

		rfa.setAppend(true);
		rfa.setPrudent(safetyMode);
		rfa.setContext(loggerContext);

		final TimeBasedRollingPolicy<ILoggingEvent> tbrp = new TimeBasedRollingPolicy<>();

		tbrp.setContext(loggerContext);
		tbrp.setFileNamePattern(filename + "-%d{" + DATE_PATTERN + "}.log");
		tbrp.setParent(rfa);
		tbrp.start();

		rfa.setRollingPolicy(tbrp);

		rfa.start();

		final ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		root.addAppender(rfa);

		return loggerContext;
	}

	static void writeContinously(final String stamp, final String filename, final boolean safetyMode) throws Exception {
		final LoggerContext lc = buildLoggerContext(stamp, filename, safetyMode);
		final Logger logger = lc.getLogger(SafeModeRollingFileAppender.class);

		final long before = System.nanoTime();
		for (int i = 0; i < LEN; i++) {
			logger.debug(LoggingThread.msgLong + " " + i);
		}
		lc.stop();
		StatusPrinter.print(lc);
		final double durationPerLog = (System.nanoTime() - before) / (LEN * 1000.0);

		System.out.println("Average duration of " + durationPerLog + " microseconds per log. Safety mode " + safetyMode);
		System.out.println("------------------------------------------------");
	}
}
