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
package ch.qos.logback.classic.joran;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogManager;

import ch.qos.logback.classic.jul.JULHelper;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.DebugUsersTurboFilter;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.StringListAppender;

import static org.junit.Assert.*;

public class JoranConfiguratorTest {

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass().getName());
  Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  @Test
  public void simpleList() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "simpleList.xml");

    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(1, listAppender.list.size());
    ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
    assertEquals(msg, le.getMessage());
  }

  @Test
  public void level() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "simpleLevel.xml");
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(0, listAppender.list.size());
  }

  @Test
  public void rootLoggerLevelSettingBySystemProperty() throws JoranException {
    String propertyName = "logback.level";

    System.setProperty(propertyName, "INFO");
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX
            + "rootLevelByProperty.xml");
    // StatusPrinter.print(loggerContext);
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(0, listAppender.list.size());
    System.clearProperty(propertyName);
  }

  @Test
  public void loggerLevelSettingBySystemProperty() throws JoranException {
    String propertyName = "logback.level";

    System.setProperty(propertyName, "DEBUG");
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX
            + "loggerLevelByProperty.xml");
    // StatusPrinter.print(loggerContext);
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(1, listAppender.list.size());
    System.clearProperty(propertyName);
  }

  @Test
  public void appenderRefSettingBySystemProperty() throws JoranException {
    final String propertyName = "logback.appenderRef";
    System.setProperty(propertyName, "A");
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX
            + "appenderRefByProperty.xml");
    final Logger logger = loggerContext
            .getLogger("ch.qos.logback.classic.joran");
    final ListAppender listAppender = (ListAppender) logger.getAppender("A");
    assertEquals(0, listAppender.list.size());
    final String msg = "hello world";
    logger.info(msg);
    assertEquals(1, listAppender.list.size());
    System.clearProperty(propertyName);
  }

  @Test
  public void statusListener() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "statusListener.xml");
    // StatusPrinter.print(loggerContext);
  }

  @Test
  public void contextRename() throws JoranException {
    loggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "contextRename.xml");
    assertEquals("wombat", loggerContext.getName());
  }

  @Test
  public void eval() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "callerData.xml");

    String msg = "hello world";
    logger.debug("toto");
    logger.debug(msg);

    StringListAppender<ILoggingEvent> slAppender = (StringListAppender<ILoggingEvent>) loggerContext
            .getLogger("root").getAppender("STR_LIST");
    assertNotNull(slAppender);
    assertEquals(2, slAppender.strList.size());
    assertTrue(slAppender.strList.get(0).contains(" DEBUG - toto"));

    String str1 = slAppender.strList.get(1);
    assertTrue(str1.contains("Caller+0"));
    assertTrue(str1.contains(" DEBUG - hello world"));
  }

  @Test
  public void turboFilter() throws JoranException {
    // Although this test uses turbo filters, it only checks
    // that Joran can see the xml element and create
    // and place the relevant object correctly.
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "turbo.xml");

    TurboFilter filter = loggerContext.getTurboFilterList().get(0);
    assertTrue(filter instanceof NOPTurboFilter);
  }

  @Test
  public void testTurboFilterWithStringList() throws JoranException {
    // Although this test uses turbo filters, it only checks
    // that Joran can see <user> elements, and behave correctly
    // that is call the addUser method and pass the correct values
    // to that method.
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "turbo2.xml");

    // StatusPrinter.print(loggerContext.getStatusManager());

    TurboFilter filter = loggerContext.getTurboFilterList().get(0);
    assertTrue(filter instanceof DebugUsersTurboFilter);
    DebugUsersTurboFilter dutf = (DebugUsersTurboFilter) filter;
    assertEquals(2, dutf.getUsers().size());
  }

  @Test
  public void testLevelFilter() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "levelFilter.xml");

    // StatusPrinter.print(loggerContext);

    logger.warn("hello");
    logger.error("to be ignored");

    @SuppressWarnings("unchecked")
    ListAppender<ILoggingEvent> listAppender = (ListAppender) root
            .getAppender("LIST");

    assertNotNull(listAppender);
    assertEquals(1, listAppender.list.size());
    ILoggingEvent back = listAppender.list.get(0);
    assertEquals(Level.WARN, back.getLevel());
    assertEquals("hello", back.getMessage());
  }

  @Test
  public void testEvaluatorFilter() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "evaluatorFilter.xml");

    // StatusPrinter.print(loggerContext);

    logger.warn("hello");
    logger.error("to be ignored");

    @SuppressWarnings("unchecked")
    ListAppender<ILoggingEvent> listAppender = (ListAppender) root
            .getAppender("LIST");

    assertNotNull(listAppender);
    assertEquals(1, listAppender.list.size());
    ILoggingEvent back = listAppender.list.get(0);
    assertEquals(Level.WARN, back.getLevel());
    assertEquals("hello", back.getMessage());
  }

  @Test
  public void testTurboDynamicThreshold() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX
            + "turboDynamicThreshold.xml");

    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());

    // this one should be denied
    MDC.put("userId", "user1");
    logger.debug("hello user1");
    // this one should log
    MDC.put("userId", "user2");
    logger.debug("hello user2");

    assertEquals(1, listAppender.list.size());
    ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
    assertEquals("hello user2", le.getMessage());
  }

  @Test
  public void testTurboDynamicThreshold2() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX
            + "turboDynamicThreshold2.xml");

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
    ILoggingEvent le = (ILoggingEvent) listAppender.list.get(0);
    assertEquals("hello user1", le.getMessage());
    le = (ILoggingEvent) listAppender.list.get(1);
    assertEquals("hello user2", le.getMessage());
  }

  // Tests whether ConfigurationAction is installing ReconfigureOnChangeFilter
  @Test
  public void scan1() throws JoranException, IOException, InterruptedException {

    String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX
            + "scan1.xml";
    configure(configFileAsStr);

    File file = new File(configFileAsStr);
    file.setLastModified(System.currentTimeMillis());

    Thread.sleep(100);
    // scanning requires 16 logs
    for (int i = 0; i < 16; i++) {
      logger.debug("after " + i);
    }

    // StatusPrinter.print(loggerContext);
    StatusChecker checker = new StatusChecker(loggerContext);
    assertTrue(checker.isErrorFree());
    assertTrue(checker.containsMatch("Resetting and reconfiguring context"));
  }

  @Test
  public void timestamp() throws JoranException, IOException,
          InterruptedException {

    String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX
            + "timestamp.xml";
    configure(configFileAsStr);

    String r = loggerContext.getProperty("testTimestamp");
    assertNotNull(r);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
    String expected = sdf.format(new Date());
    assertEquals("expected \"" + expected + "\" but got " + r, expected, r);
  }

  @Test
  public void encoderCharset() throws JoranException, IOException,
          InterruptedException {

    String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX
            + "encoderCharset.xml";
    configure(configFileAsStr);

    ConsoleAppender<ILoggingEvent> consoleAppender = (ConsoleAppender<ILoggingEvent>) root.getAppender("CONSOLE");
    assertNotNull(consoleAppender);
    LayoutWrappingEncoder<ILoggingEvent> encoder = (LayoutWrappingEncoder<ILoggingEvent>) consoleAppender.getEncoder();

    assertEquals("UTF-8", encoder.getCharset().displayName());

    StatusChecker checker = new StatusChecker(loggerContext);
    assertTrue(checker.isErrorFree());
  }

  void verifyJULLevel(String loggerName, Level expectedLevel) {
    LogManager lm = LogManager.getLogManager();

    java.util.logging.Logger julLogger = JULHelper.asJULLogger(loggerName);

    java.util.logging.Level julLevel = julLogger.getLevel();

    if (expectedLevel == null) {
      assertNull(julLevel);
    } else {
      assertEquals(JULHelper.asJULLevel(expectedLevel), julLevel);
    }


  }

  @Test
  public void levelChangePropagator0() throws JoranException, IOException,
          InterruptedException {
    java.util.logging.Logger.getLogger("xx").setLevel(java.util.logging.Level.INFO);
    String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX
            + "/jul/levelChangePropagator0.xml";
    configure(configFileAsStr);
    StatusChecker checker = new StatusChecker(loggerContext);
    assertTrue(checker.isErrorFree());
    verifyJULLevel("xx", null);
    verifyJULLevel("a.b.c", Level.WARN);
    verifyJULLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
  }

  @Test
  public void levelChangePropagator1() throws JoranException, IOException,
          InterruptedException {
    java.util.logging.Logger.getLogger("xx").setLevel(java.util.logging.Level.INFO);
    String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX
            + "/jul/levelChangePropagator1.xml";
    configure(configFileAsStr);
    StatusChecker checker = new StatusChecker(loggerContext);
    assertTrue(checker.isErrorFree());
    verifyJULLevel("xx", Level.INFO);
    verifyJULLevel("a.b.c", Level.WARN);
    verifyJULLevel(Logger.ROOT_LOGGER_NAME, Level.TRACE);
  }


}
