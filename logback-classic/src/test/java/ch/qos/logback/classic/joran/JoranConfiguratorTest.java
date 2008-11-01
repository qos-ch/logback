/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.StringListAppender;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.DebugUsersTurboFilter;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class JoranConfiguratorTest {

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass().getName());
  Logger root = loggerContext.getLogger(LoggerContext.ROOT_NAME);

  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  @Test
  public void testSimpleList() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/simpleList.xml");

    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(LoggerContext.ROOT_NAME);
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(1, listAppender.list.size());
    LoggingEvent le = (LoggingEvent) listAppender.list.get(0);
    assertEquals(msg, le.getMessage());
  }

  @Test
  public void testLevel() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/simpleLevel.xml");
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(0, listAppender.list.size());
  }

  @Test
  public void testStatusListener() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/statusListener.xml");

    StatusPrinter.print(loggerContext);
  }

  @Test
  public void testEval() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/callerData.xml");

    String msg = "hello world";
    logger.debug("toto");
    logger.debug(msg);

    StringListAppender slAppender = (StringListAppender) loggerContext
        .getLogger("root").getAppender("STR_LIST");
    assertNotNull(slAppender);
    assertEquals(2, slAppender.strList.size());
    assertTrue(slAppender.strList.get(0).contains(" DEBUG - toto"));

    String str1 = slAppender.strList.get(1);
    assertTrue(str1.contains("Caller+0"));
    assertTrue(str1.contains(" DEBUG - hello world"));
  }

  @Test
  public void testTurboFilter() throws JoranException {
    // Although this test uses turbo filters, it only checks
    // that Joran can see the xml element and create
    // and place the relevant object correctly.
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/turbo.xml");

    TurboFilter filter = loggerContext.getTurboFilterAI().get(0);
    assertTrue(filter instanceof NOPTurboFilter);
  }

  @Test
  public void testTurboFilterWithStringList() throws JoranException {
    // Although this test uses turbo filters, it only checks
    // that Joran can see <user> elements, and behave correctly
    // that is call the addUser method and pass the correct values
    // to that method.
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/turbo2.xml");

    // StatusPrinter.print(loggerContext.getStatusManager());

    TurboFilter filter = loggerContext.getTurboFilterAI().get(0);
    assertTrue(filter instanceof DebugUsersTurboFilter);
    DebugUsersTurboFilter dutf = (DebugUsersTurboFilter) filter;
    assertEquals(2, dutf.getUsers().size());
  }

  @Test
  public void testLevelFilter() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/levelFilter.xml");

    StatusPrinter.print(loggerContext);

    logger.warn("hello");
    logger.error("to be ignored");

    @SuppressWarnings("unchecked")
    ListAppender<LoggingEvent> listAppender = (ListAppender) root
        .getAppender("LIST");

    assertNotNull(listAppender);
    assertEquals(1, listAppender.list.size());
    LoggingEvent back = listAppender.list.get(0);
    assertEquals(Level.WARN, back.getLevel());
    assertEquals("hello", back.getMessage());
  }

  @Test
  public void testEvaluatorFilter() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/evaluatorFilter.xml");

    StatusPrinter.print(loggerContext);

    logger.warn("hello");
    logger.error("to be ignored");

    @SuppressWarnings("unchecked")
    ListAppender<LoggingEvent> listAppender = (ListAppender) root
        .getAppender("LIST");

    assertNotNull(listAppender);
    assertEquals(1, listAppender.list.size());
    LoggingEvent back = listAppender.list.get(0);
    assertEquals(Level.WARN, back.getLevel());
    assertEquals("hello", back.getMessage());
  }

  @Test
  public void testTurboDynamicThreshold() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX
        + "input/joran/turboDynamicThreshold.xml");

    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());

    // this one should be denied
    MDC.put("userId", "user1");
    logger.debug("hello user1");
    // this one should log
    MDC.put("userId", "user2");
    logger.debug("hello user2");

    assertEquals(1, listAppender.list.size());
    LoggingEvent le = (LoggingEvent) listAppender.list.get(0);
    assertEquals("hello user2", le.getMessage());
  }

  @Test
  public void testTurboDynamicThreshold2() throws JoranException {
    configure(TeztConstants.TEST_DIR_PREFIX
        + "input/joran/turboDynamicThreshold2.xml");

    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());

    // this one should log
    MDC.put("userId", "user1");
    logger.debug("hello user1");
    // this one should log
    MDC.put("userId", "user2");
    logger.debug("hello user2");
    // this one should fail
    MDC.put("userId", "user3");
    logger.debug("hello user3");

    assertEquals(2, listAppender.list.size());
    LoggingEvent le = (LoggingEvent) listAppender.list.get(0);
    assertEquals("hello user1", le.getMessage());
    le = (LoggingEvent) listAppender.list.get(1);
    assertEquals("hello user2", le.getMessage());
  }
}
