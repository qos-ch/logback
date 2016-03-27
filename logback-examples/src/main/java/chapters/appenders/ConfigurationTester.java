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
package chapters.appenders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import chapters.appenders.sub.sample.Bar;

/**
 * 
 * This java "application" configures logback using a configuration file
 * supplied on the class path and logs a few times.
 * 
 * @author S&eacute;bastien Pennec
 */
public class ConfigurationTester {

    public static void main(String[] args) throws InterruptedException {
        Logger logger = (Logger) LoggerFactory.getLogger(ConfigurationTester.class);
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure(args[0]);
        } catch (JoranException je) {
            je.printStackTrace();
        }
        // Next call forces status messages to be printed on the console
        // StatusPrinter.print(lc);

        logger.debug("**Hello {}", new Bar());
        MDC.put("testKey", "testValueFromMDC");
        MDC.put("testKey2", "value2");
        for (int i = 0; i < 64; i++) {
            logger.debug("logging statement {}", i);
            Thread.sleep(100);
        }
        Bar bar = new Bar();
        bar.createLoggingRequest();
    }
}
