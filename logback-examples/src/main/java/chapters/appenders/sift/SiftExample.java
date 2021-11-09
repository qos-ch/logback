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
package chapters.appenders.sift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class SiftExample {

    public static void main(final String[] args) throws JoranException {
        if (args.length != 1) {
            usage("Wrong number of arguments.");
        }

        final String configFile = args[0];

        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        lc.reset();
        configurator.setContext(lc);
        configurator.doConfigure(configFile);

        final Logger logger = LoggerFactory.getLogger(SiftExample.class);
        logger.debug("Application started");

        MDC.put("userid", "Alice");
        logger.debug("Alice says hello");

        // StatusPrinter.print(lc);
    }

    static void usage(final String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + SiftExample.class.getName() + " configFile\n" + "   configFile a logback configuration file");
        System.exit(1);
    }
}
