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
package ch.qos.logback.classic.sift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class SiftingAppenderTest {

  static String PREFIX = TeztConstants.TEST_DIR_PREFIX + "input/joran/sift/";

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass().getName());
  Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  @Test
  public void unsetDefaultValueProperty() throws JoranException {
    configure(PREFIX + "unsetDefaultValueProperty.xml");
    logger.debug("hello");
    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    assertFalse(sa.isStarted());
  }

  @Test
  public void smoke() throws JoranException {
    configure(PREFIX + "smoke.xml");
    logger.debug("smoke");
    long timestamp = 0;
    SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
    ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) ha
        .getAppenderTracker().get("smoke", timestamp);

    StatusPrinter.print(loggerContext);
    assertNotNull(listAppender);
    List<ILoggingEvent> eventList = listAppender.list;
    assertEquals(1, listAppender.list.size());
    assertEquals("smoke", eventList.get(0).getMessage());
  }

  @Test
  public void defaultLayoutRule() throws JoranException {
    configure(PREFIX + "defaultLayoutRule.xml");
    logger.debug("hello");
    long timestamp = 0;
    SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
    StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) ha
        .getAppenderTracker().get("default", timestamp);

    StatusPrinter.print(loggerContext);
    assertNotNull(listAppender);
    List<String> strList = listAppender.strList;
    assertEquals(1, strList.size());
    assertEquals("DEBUG hello", strList.get(0));
  }

  @Test
  public void testWholeCycle() throws JoranException {
    String mdcKey = "cycle";
    configure(PREFIX + "completeCycle.xml");
    MDC.put(mdcKey, "a");
    logger.debug("smoke");
    long timestamp = System.currentTimeMillis();
    SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
    ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) ha
        .getAppenderTracker().get("a", timestamp);
    StatusPrinter.print(loggerContext);

    assertNotNull(listAppender);
    List<ILoggingEvent> eventList = listAppender.list;
    assertEquals(1, listAppender.list.size());
    assertEquals("smoke", eventList.get(0).getMessage());

    MDC.remove(mdcKey);
    LoggingEvent le = new LoggingEvent("x", logger, Level.INFO, "hello", null,
        null);
    le.setTimeStamp(timestamp + AppenderTracker.THRESHOLD * 2);
    ha.doAppend(le);
    assertFalse(listAppender.isStarted());
    assertEquals(1, ha.getAppenderTracker().keyList().size());
    assertEquals("cycleDefault", ha.getAppenderTracker().keyList().get(0));
  }
}
