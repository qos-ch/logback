/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.TestConstants;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.mock.MockSyslogServer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.net.SyslogConstants;
import ch.qos.logback.core.testUtil.RandomUtil;

public class SyslogAppenderTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void basic() throws InterruptedException {
    int port = RandomUtil.getRandomServerPort();

    MockSyslogServer mockServer = new MockSyslogServer(1, port);
    mockServer.start();
    // give MockSyslogServer head start
    Thread.sleep(100);

    LoggerContext lc = new LoggerContext();
    lc.setName("test");
    SyslogAppender sa = new SyslogAppender();
    sa.setContext(lc);
    sa.setSyslogHost("localhost");
    sa.setFacility("MAIL");
    sa.setPort(port);
    sa.setSuffixPattern("[%thread] %logger %msg");
    sa.start();
    assertTrue(sa.isStarted());

    String loggerName = this.getClass().getName();
    Logger logger = lc.getLogger(loggerName);
    logger.addAppender(sa);
    String logMsg = "hello";
    logger.debug(logMsg);

    // wait max 2 seconds for mock server to finish. However, it should
    // much sooner than that.
    mockServer.join(8000);
    assertTrue(mockServer.isFinished());
    assertEquals(1, mockServer.getMessageList().size());
    String msg = mockServer.getMessageList().get(0);

    String threadName = Thread.currentThread().getName();

    String expected = "<"
        + (SyslogConstants.LOG_MAIL + SyslogConstants.DEBUG_SEVERITY) + ">";
    assertTrue(msg.startsWith(expected));

    String first = "<\\d{2}>\\w{3} \\d{2} \\d{2}(:\\d{2}){2} [\\w.]* ";
    assertTrue(msg.matches(first + "\\[" + threadName + "\\] " + loggerName
        + " " + logMsg));

  }

  @Test
  public void tException() throws InterruptedException {
    int port = RandomUtil.getRandomServerPort();

    MockSyslogServer mockServer = new MockSyslogServer(21, port);
    mockServer.start();
    // give MockSyslogServer head start
    Thread.sleep(100);

    LoggerContext lc = new LoggerContext();
    lc.setName("test");
    SyslogAppender sa = new SyslogAppender();
    sa.setContext(lc);
    sa.setSyslogHost("localhost");
    sa.setFacility("MAIL");
    sa.setPort(port);
    sa.setSuffixPattern("[%thread] %logger %msg");
    sa.start();
    assertTrue(sa.isStarted());

    String loggerName = this.getClass().getName();
    Logger logger = lc.getLogger(loggerName);
    logger.addAppender(sa);
    String logMsg = "hello";
    String exMsg = "just testing";
    Exception ex = new Exception(exMsg);
    logger.debug(logMsg, ex);
    // StatusPrinter.print(lc.getStatusManager());

    // wait max 2 seconds for mock server to finish. However, it should
    // much sooner than that.
    mockServer.join(8000);
    assertTrue(mockServer.isFinished());

    // message + 20 lines of stacktrace
    assertEquals(21, mockServer.getMessageList().size());
    // int i = 0;
    // for (String line: mockServer.msgList) {
    // System.out.println(i++ + ": " + line);
    // }

    String msg = mockServer.getMessageList().get(0);
    String expected = "<"
        + (SyslogConstants.LOG_MAIL + SyslogConstants.DEBUG_SEVERITY) + ">";
    assertTrue(msg.startsWith(expected));

    String expectedPrefix = "<\\d{2}>\\w{3} \\d{2} \\d{2}(:\\d{2}){2} [\\w.]* ";
    String threadName = Thread.currentThread().getName();
    String expectedResult = expectedPrefix + "\\[" + threadName + "\\] "
        + loggerName + " " + logMsg;
    assertTrue(msg.matches(expectedResult));
  }

  @Test
  public void LBCLASSIC_50() throws JoranException {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    lc.shutdownAndReset();
    configurator.doConfigure(TestConstants.JORAN_ONPUT_PREFIX
        + "/syslog_LBCLASSIC_50.xml");

    org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.info("hello");
  }
}
