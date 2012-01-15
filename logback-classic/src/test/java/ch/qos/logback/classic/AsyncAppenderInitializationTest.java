/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;

public class AsyncAppenderInitializationTest {

  org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncAppenderInitializationTest.class);
  LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
  Logger root = (Logger) LoggerFactory.getLogger("root");

  @Before
  public void setUp() throws Exception {
      logger.info("AsyncAppenderInitializationTest");
  }

  @After
  public void tearDown() throws Exception {
      System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
      lc.reset(); // we are going to need this context
  }

  @Test
  public void testLogbackInitialization() throws JoranException {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "AsyncAppender_logback-test.xml");
    new ContextInitializer(lc).autoConfig();
    Appender appender = root.getAppender("ASYNC");
    assertNotNull(appender);
    //StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    //StatusManager sm = lc.getStatusManager();
    //assertEquals("Was expecting no errors", 0, sm.getLevel());
  }
}
