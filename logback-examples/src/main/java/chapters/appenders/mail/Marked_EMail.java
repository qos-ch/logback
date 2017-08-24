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
package chapters.appenders.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This application generates a number of message many of which are of LEVEL.
 * However, only one message bears the  "NOTIFY_ADMIN" marker.
 * */
public class Marked_EMail {
    static public void main(String[] args) throws Exception {
        if (args.length != 1) {
            usage("Wrong number of arguments.");
        }

        String configFile = args[0];

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        lc.reset();
        configurator.setContext(lc);
        configurator.doConfigure(configFile);
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

        Logger logger = LoggerFactory.getLogger(Marked_EMail.class);

        int runLength = 100;
        for (int i = 1; i <= runLength; i++) {
            if ((i % 10) < 9) {
                logger.debug("This is a debug message. Message number: " + i);
            } else {
                logger.error("This is an error message. Message number: " + i);
            }
        }

        Marker notifyAdmin = MarkerFactory.getMarker("NOTIFY_ADMIN");
        logger.error(notifyAdmin, "This is a serious an error requiring the admin's attention", new Exception("Just testing"));

        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

    static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java " + Marked_EMail.class.getName() + " configFile\n" + "   configFile a logback configuration file in XML format.");
        System.exit(1);
    }
}
