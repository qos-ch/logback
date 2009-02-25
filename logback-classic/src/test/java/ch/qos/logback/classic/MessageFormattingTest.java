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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class MessageFormattingTest  {

  LoggerContext lc;
  ListAppender<ILoggingEvent> listAppender;

  @Before
  public void setUp() {
    lc = new LoggerContext();
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    listAppender = new ListAppender<ILoggingEvent>();
    listAppender.setContext(lc);
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @Test
  public void testFormattingOneArg() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("{}", new Integer(12));
    ILoggingEvent event = (ILoggingEvent) listAppender.list.get(0);
    assertEquals("12", event.getFormattedMessage());
  }

  @Test
  public void testFormattingTwoArg() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("{}-{}", new Integer(12), new Integer(13));
    ILoggingEvent event = (ILoggingEvent) listAppender.list.get(0);
    assertEquals("12-13", event.getFormattedMessage());
  }

  @Test
  public void testNoFormatting() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test", new Integer(12), new Integer(13));
    ILoggingEvent event = (ILoggingEvent) listAppender.list.get(0);
    assertEquals("test", event.getFormattedMessage());
  }

  @Test
  public void testNoFormatting2() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test");
    ILoggingEvent event = (ILoggingEvent) listAppender.list.get(0);
    assertEquals("test", event.getFormattedMessage());
  }

  @Test
  public void testMessageConverter() {
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("{}", 12);
    ILoggingEvent event = (ILoggingEvent) listAppender.list.get(0);
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setPattern("%m");
    layout.start();
    String formattedMessage = layout.doLayout(event);
    assertEquals("12", formattedMessage);
  }

}
