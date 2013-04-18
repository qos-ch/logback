/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
import ch.qos.logback.core.util.StatusPrinter;
import chapters.appenders.sub.sample.Bar;

/**
 * 
 * This class can be used to check the result of a configuration file.
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
    // After we've called Joran, let's print information about the
    // internal status of logback
    StatusPrinter.print(lc);

    logger.debug("**Hello {}", new Bar());
    MDC.put("testKey", "testValueFromMDC");
    MDC.put("testKey2", "value2");
    for (int i = 0; i < 10; i++) {
      logger.debug("logging statement " + i);
      Thread.sleep(100);
    }
    Bar bar = new Bar();
    bar.createLoggingRequest();
  }
}
