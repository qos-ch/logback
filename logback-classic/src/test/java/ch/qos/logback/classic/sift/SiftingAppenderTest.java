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
package ch.qos.logback.classic.sift;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.List;

import static org.junit.Assert.*;

public class SiftingAppenderTest {

  static String SIFT_FOLDER_PREFIX = ClassicTestConstants.JORAN_INPUT_PREFIX + "sift/";

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass().getName());
  Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
  StatusChecker sc = new StatusChecker(loggerContext);
  int diff = RandomUtil.getPositiveInt();

  protected void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  @Test
  public void unsetDefaultValueProperty() throws JoranException {
    configure(SIFT_FOLDER_PREFIX + "unsetDefaultValueProperty.xml");
    logger.debug("hello");
    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    assertFalse(sa.isStarted());
  }

  @Test
  public void smoke() throws JoranException {
    configure(SIFT_FOLDER_PREFIX + "smoke.xml");
    logger.debug("smoke");
    long timestamp = 0;
    SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
    ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) ha
            .getAppenderTracker().get("smoke", timestamp);
    assertNotNull(listAppender);
    List<ILoggingEvent> eventList = listAppender.list;
    assertEquals(1, listAppender.list.size());
    assertEquals("smoke", eventList.get(0).getMessage());
  }

  @Test
  public void zeroNesting() throws JoranException {
    configure(SIFT_FOLDER_PREFIX + "zeroNesting.xml");
    logger.debug("hello");
    logger.debug("hello");
    logger.debug("hello");
    logger.debug("hello");
    logger.debug("hello");

    long timestamp = 0;

    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    NOPAppender<ILoggingEvent> nopa = (NOPAppender<ILoggingEvent>) sa
            .getAppenderTracker().get("smoke", timestamp);
    StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);

    assertNotNull(nopa);
    sc.assertContainsMatch(ErrorStatus.ERROR, "No nested appenders found");
    sc.assertContainsMatch(ErrorStatus.ERROR,
            "Failed to build an appender for discriminating value \\[smoke\\]");
  }

  @Test
  public void multipleNesting() throws JoranException {
    configure(SIFT_FOLDER_PREFIX + "multipleNesting.xml");
    logger.debug("hello");
    logger.debug("hello");
    logger.debug("hello");

    long timestamp = 0;

    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) sa
            .getAppenderTracker().get("smoke", timestamp);
    StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);

    assertNotNull(listAppender);
    sc.assertContainsMatch(ErrorStatus.ERROR,
            "Only and only one appender can be nested");
  }

  @Test
  public void defaultLayoutRule() throws JoranException {
    configure(SIFT_FOLDER_PREFIX + "defaultLayoutRule.xml");
    logger.debug("hello");
    long timestamp = 0;
    SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
    StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) ha
            .getAppenderTracker().get("default", timestamp);

    assertNotNull(listAppender);
    List<String> strList = listAppender.strList;
    assertEquals(1, strList.size());
    assertEquals("DEBUG hello", strList.get(0));
  }

  @Test
  public void testWholeCycle() throws JoranException {
    String mdcKey = "cycle";
    configure(SIFT_FOLDER_PREFIX + "completeCycle.xml");
    MDC.put(mdcKey, "a");
    logger.debug("smoke");
    long timestamp = System.currentTimeMillis();
    SiftingAppender ha = (SiftingAppender) root.getAppender("SIFT");
    ListAppender<ILoggingEvent> listAppender = (ListAppender<ILoggingEvent>) ha
            .getAppenderTracker().get("a", timestamp);
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

  @Test
  public void localPropertiesShouldBeVisible() throws JoranException {
    String mdcKey = "localProperty";
    String mdcVal = "" + diff;
    String msg = "localPropertiesShouldBeVisible";
    String prefix = "Y";
    configure(SIFT_FOLDER_PREFIX + "propertyPropagation.xml");
    MDC.put(mdcKey, mdcVal);
    logger.debug(msg);
    long timestamp = System.currentTimeMillis();
    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) sa
            .getAppenderTracker().get(mdcVal, timestamp);
    assertNotNull(listAppender);
    List<String> strList = listAppender.strList;
    assertEquals(1, listAppender.strList.size());
    assertEquals(prefix + msg, strList.get(0));
  }

  @Test
  public void propertyDefinedWithinSiftElementShouldBeVisible() throws JoranException {
    String mdcKey = "propertyDefinedWithinSift";
    String mdcVal = "" + diff;
    String msg = "propertyDefinedWithinSiftElementShouldBeVisible";
    String prefix = "Y";
    configure(SIFT_FOLDER_PREFIX + "propertyDefinedInSiftElement.xml");
    MDC.put(mdcKey, mdcVal);
    logger.debug(msg);
    long timestamp = System.currentTimeMillis();
    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) sa
            .getAppenderTracker().get(mdcVal, timestamp);
    assertNotNull(listAppender);
    List<String> strList = listAppender.strList;
    assertEquals(1, listAppender.strList.size());
    assertEquals(prefix + msg, strList.get(0));
  }

  @Test
  public void compositePropertyShouldCombineWithinAndWithoutSiftElement() throws JoranException {
    String mdcKey = "compositeProperty";
    String mdcVal = "" + diff;
    String msg = "compositePropertyShouldCombineWithinAndWithoutSiftElement";
    String prefix = "composite";
    configure(SIFT_FOLDER_PREFIX + "compositeProperty.xml");
    MDC.put(mdcKey, mdcVal);
    logger.debug(msg);
    long timestamp = System.currentTimeMillis();
    SiftingAppender sa = (SiftingAppender) root.getAppender("SIFT");
    StringListAppender<ILoggingEvent> listAppender = (StringListAppender<ILoggingEvent>) sa
            .getAppenderTracker().get(mdcVal, timestamp);
    assertNotNull(listAppender);
    List<String> strList = listAppender.strList;
    assertEquals(1, listAppender.strList.size());
    assertEquals(prefix + msg, strList.get(0));
  }


}
