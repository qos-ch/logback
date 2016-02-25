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
package ch.qos.logback.classic.issue.lbclassic323;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class Simple {

    static Logger logger = LoggerFactory.getLogger(Simple.class);
    static String DIR_PREFIX = "src/test/java/ch/qos/logback/classic/issue/lbclassic323/";

    public static void main(String[] args) throws JoranException, InterruptedException {
        init(DIR_PREFIX + "logback_smtp.xml");

        for (int i = 0; i < 10; i++) {
            logger.debug("SEE IF THIS IS LOGGED {}.", i);
        }
        logger.error("trigger");
        System.out.println("done");
        System.exit(0);
    }

    static void init(String file) throws JoranException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.reset();
        jc.doConfigure(file);
    }

}
