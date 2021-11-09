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
package ch.qos.logback.classic.issue.lbclassic330;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);
    static String DIR_PREFIX = "src/test/java/ch/qos/logback/classic/issue/lbclassic330/";

    public static void main(final String[] args) throws JoranException, InterruptedException {
        init(DIR_PREFIX + "logback.xml");
        logger.debug("hello");
    }

    static void init(final String file) throws JoranException {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.reset();
        jc.doConfigure(file);
    }
}
