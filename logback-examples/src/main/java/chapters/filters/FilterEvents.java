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
package chapters.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class FilterEvents {

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("A configuration file must be passed as a parameter.");
            return;
        }

        Logger logger = (Logger) LoggerFactory.getLogger(FilterEvents.class);
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure(args[0]);
        } catch (JoranException je) {
            je.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            if (i == 3) {
                MDC.put("username", "sebastien");
                logger.debug("logging statement {}", i);
                MDC.remove("username");
            } else if (i == 6) {
                Marker billing = MarkerFactory.getMarker("billing");
                logger.error(billing, "billing statement {}", i);
            } else {
                logger.info("logging statement {}", i);
            }
        }
    }
}
