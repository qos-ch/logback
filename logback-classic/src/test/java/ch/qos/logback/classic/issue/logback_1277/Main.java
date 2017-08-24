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
package ch.qos.logback.classic.issue.logback_1277;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);
    static String CONFIG_FILE = ClassicTestConstants.ISSUES_PREFIX + "logback-1277.xml";

    public static void main(String[] args) throws JoranException, InterruptedException {
        init(CONFIG_FILE);
        int runLen = 1000 * 1000;
        for (int i = 0; i < runLen; i++) {
            logger.debug("hello");
        }
        System.out.println("Will sleep for 60 seconds");
        Thread.sleep(1000 * 60);
        System.out.println("Exiting");

    }

    static void init(String file) throws JoranException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.reset();
        jc.doConfigure(file);
    }
}
