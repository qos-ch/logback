/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2007, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import junit.framework.TestCase;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class MessageFormattingTest extends TestCase {

  LoggerContext lc;
  ListAppender<LoggingEvent> listAppender;

  public void setUp() {
    lc = new LoggerContext();
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    listAppender = new ListAppender<LoggingEvent>();
    listAppender.setContext(lc);
    listAppender.start();
    logger.addAppender(listAppender);
  }

  public void testFormattingOneArg() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("{}", new Integer(12));
    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    assertEquals("12", event.getFormattedMessage());
  }

  public void testFormattingTwoArg() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("{}-{}", new Integer(12), new Integer(13));
    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    assertEquals("12-13", event.getFormattedMessage());
  }

  public void testNoFormatting() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test", new Integer(12), new Integer(13));
    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    assertEquals("test", event.getFormattedMessage());
  }

  public void testNoFormatting2() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test");
    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    assertEquals("test", event.getFormattedMessage());
  }

  public void testMessageConverter() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("{}", 12);
    LoggingEvent event = (LoggingEvent) listAppender.list.get(0);
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setPattern("%m");
    layout.start();
    String formattedMessage = layout.doLayout(event);
    assertEquals("12", formattedMessage);
  }

}
