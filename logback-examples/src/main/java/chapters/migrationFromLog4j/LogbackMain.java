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
package chapters.migrationFromLog4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A minimal application making use of logback-classic. It uses the
 * configuration file logback-trivial.xml which makes use of
 * TrivialLogbackAppender.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LogbackMain {

    static Logger logger = LoggerFactory.getLogger(LogbackMain.class);

    public static void main(String[] args) throws JoranException {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure("src/main/java/chapters/migrationFromLog4j/logback-trivial.xml");
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

        logger.debug("Hello world");
    }

}
