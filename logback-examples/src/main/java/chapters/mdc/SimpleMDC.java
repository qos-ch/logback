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
package chapters.mdc;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;

public class SimpleMDC {
    static public void main(String[] args) throws Exception {
        // You can put values in the MDC at any time. Before anything else
        // we put the first name
        MDC.put("first", "Dorothy");

        // configure via the configuration file "chapters/mdc/simpleMDC.xml"
        // which ships with the examples
        configureViaXML_File();

        // For educational purposes, the same configuration can
        // be accomplished programmatically.
        //
        // programmaticConfiguration();

        Logger logger = LoggerFactory.getLogger(SimpleMDC.class);
        // We now put the last name
        MDC.put("last", "Parker");

        // The most beautiful two words in the English language according
        // to Dorothy Parker:
        logger.info("Check enclosed.");
        logger.debug("The most beautiful two words in English.");

        MDC.put("first", "Richard");
        MDC.put("last", "Nixon");
        logger.info("I am not a crook.");
        logger.info("Attributed to the former US president. 17 Nov 1973.");
    }

    static void programmaticConfiguration() {
        // Configure logback
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        PatternLayoutEncoder layout = new PatternLayoutEncoder();
        layout.setContext(loggerContext);
        layout.setPattern("%X{first} %X{last} - %m%n");
        layout.start();
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(loggerContext);
        appender.setEncoder(layout);
        appender.start();
        // cast root logger to c.q.logback.classic.Logger so that we can attach
        // an appender to it
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root");
        root.addAppender(appender);
    }

    static void configureViaXML_File() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            URL url = Loader.getResourceBySelfClassLoader("chapters/mdc/simpleMDC.xml");
            configurator.doConfigure(url);
        } catch (JoranException je) {
            StatusPrinter.print(lc);
        }
    }

}
