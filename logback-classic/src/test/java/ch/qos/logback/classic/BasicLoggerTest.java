/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import ch.qos.logback.core.appender.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class BasicLoggerTest extends TestCase {

  public void testBasic() {
    LoggerContext lc = new LoggerContext();
    ListAppender listAppender = new ListAppender();
    listAppender.start();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.addAppender(listAppender);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertEquals(0, listAppender.list.size());
    logger.debug("hello");
    assertEquals(1, listAppender.list.size());
  }

  public void testNoStart() {
    LoggerContext lc = new LoggerContext();
    ListAppender listAppender = new ListAppender();
    // listAppender.start();
    listAppender.setContext(lc);
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.addAppender(listAppender);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    logger.debug("hello");
    StatusPrinter.print(lc.getStatusManager());
  }

  public void testAdditive() {
    LoggerContext lc = new LoggerContext();
    ListAppender listAppender = new ListAppender();
    listAppender.start();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.addAppender(listAppender);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    logger.addAppender(listAppender);
    logger.setAdditive(false);
    logger.debug("hello");
    // 1 instead of two, since logger is not additive
    assertEquals(1, listAppender.list.size());
  }

  public void testRootLogger() {
    Logger logger = (Logger) LoggerFactory.getLogger(LoggerContext.ROOT_NAME);
    LoggerContext lc = logger.getLoggerContext();

    assertNotNull("Returned logger is null", logger);
    assertEquals("Return logger isn't named root", logger.getName(),
        LoggerContext.ROOT_NAME);
    assertTrue("logger instances should be indentical", logger == lc.root);
  }
  
  public void testBasicFiltering() throws Exception {
    LoggerContext lc = new LoggerContext();
    ListAppender listAppender = new ListAppender();
    listAppender.start();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.addAppender(listAppender);
    root.setLevel(Level.INFO);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    logger.debug("x");
    assertEquals(0, listAppender.list.size());
    logger.info("x");
    logger.warn("x");
    logger.error("x");
    assertEquals(3, listAppender.list.size());
  }
  
}
