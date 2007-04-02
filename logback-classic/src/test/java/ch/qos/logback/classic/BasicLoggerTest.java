/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class BasicLoggerTest extends TestCase {

  public void testBasic() {
    LoggerContext lc = new LoggerContext();
    ListAppender<LoggingEvent> listAppender = new ListAppender<LoggingEvent>();
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
    ListAppender<LoggingEvent> listAppender = new ListAppender<LoggingEvent>();
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
    ListAppender<LoggingEvent> listAppender = new ListAppender<LoggingEvent>();
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
    ListAppender<LoggingEvent> listAppender = new ListAppender<LoggingEvent>();
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

  public void testEnabledX_All() throws Exception {
    LoggerContext lc = new LoggerContext();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.setLevel(Level.ALL);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertTrue(logger.isDebugEnabled());
    assertTrue(logger.isInfoEnabled());
    assertTrue(logger.isWarnEnabled());
    assertTrue(logger.isErrorEnabled());
    assertTrue(logger.isEnabledFor(Level.DEBUG));
    assertTrue(logger.isEnabledFor(Level.INFO));
    assertTrue(logger.isEnabledFor(Level.WARN));    
    assertTrue(logger.isEnabledFor(Level.ERROR));
  }
  
  public void testEnabledX_Debug() throws Exception {
    LoggerContext lc = new LoggerContext();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.setLevel(Level.DEBUG);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertTrue(logger.isDebugEnabled());
    assertTrue(logger.isInfoEnabled());
    assertTrue(logger.isWarnEnabled());
    assertTrue(logger.isErrorEnabled());
    assertTrue(logger.isEnabledFor(Level.DEBUG));
    assertTrue(logger.isEnabledFor(Level.INFO));
    assertTrue(logger.isEnabledFor(Level.WARN));    
    assertTrue(logger.isEnabledFor(Level.ERROR));
  }
  
  
  
  public void testEnabledX_Info() throws Exception {
    LoggerContext lc = new LoggerContext();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.setLevel(Level.INFO);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertFalse(logger.isDebugEnabled());
    assertTrue(logger.isInfoEnabled());
    assertTrue(logger.isWarnEnabled());
    assertTrue(logger.isErrorEnabled());
    assertFalse(logger.isEnabledFor(Level.DEBUG));
    assertTrue(logger.isEnabledFor(Level.INFO));
    assertTrue(logger.isEnabledFor(Level.WARN));    
    assertTrue(logger.isEnabledFor(Level.ERROR));
  }
  
  public void testEnabledX_Warn() throws Exception {
    LoggerContext lc = new LoggerContext();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.setLevel(Level.WARN);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertFalse(logger.isDebugEnabled());
    assertFalse(logger.isInfoEnabled());
    assertTrue(logger.isWarnEnabled());
    assertTrue(logger.isErrorEnabled());
    assertFalse(logger.isEnabledFor(Level.DEBUG));
    assertFalse(logger.isEnabledFor(Level.INFO));
    assertTrue(logger.isEnabledFor(Level.WARN));    
    assertTrue(logger.isEnabledFor(Level.ERROR));
  }
  
  public void testEnabledX_Errror() throws Exception {
    LoggerContext lc = new LoggerContext();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.setLevel(Level.ERROR);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertFalse(logger.isDebugEnabled());
    assertFalse(logger.isInfoEnabled());
    assertFalse(logger.isWarnEnabled());
    assertTrue(logger.isErrorEnabled());
    assertFalse(logger.isEnabledFor(Level.DEBUG));
    assertFalse(logger.isEnabledFor(Level.INFO));
    assertFalse(logger.isEnabledFor(Level.WARN));    
    assertTrue(logger.isEnabledFor(Level.ERROR));
  }

  public void testEnabledX_Off() throws Exception {
    LoggerContext lc = new LoggerContext();
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    root.setLevel(Level.OFF);
    Logger logger = lc.getLogger(BasicLoggerTest.class);
    assertFalse(logger.isDebugEnabled());
    assertFalse(logger.isInfoEnabled());
    assertFalse(logger.isWarnEnabled());
    assertFalse(logger.isErrorEnabled());
    assertFalse(logger.isEnabledFor(Level.DEBUG));
    assertFalse(logger.isEnabledFor(Level.INFO));
    assertFalse(logger.isEnabledFor(Level.WARN));    
    assertFalse(logger.isEnabledFor(Level.ERROR));
  }
}
