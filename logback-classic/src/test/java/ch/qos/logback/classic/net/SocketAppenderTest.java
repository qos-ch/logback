/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.net;

import java.util.Map;

import junit.framework.TestCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEvent;

public class SocketAppenderTest extends TestCase {

  private LoggerContext lc;
  private MockSocketServer mockSocketServer;

  public void testStartFailNoRemoteHost() {
    lc = new LoggerContext();
    SocketAppender appender = new SocketAppender();
    appender.setContext(lc);
    appender.setPort(123);
    appender.start();
    assertEquals(1, lc.getStatusManager().getCount());
  }

  public void testRecieveMessage() throws InterruptedException {
    startServer(1);
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test msg");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    mockSocketServer.join(2000);
    assertTrue(mockSocketServer.finished);
    assertEquals(1, mockSocketServer.loggingEventList.size());

    LoggingEvent remoteEvent = mockSocketServer.loggingEventList.get(0);
    assertEquals("test msg", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  public void testRecieveWithContext() throws InterruptedException {
    startServer(1);
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test msg");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    mockSocketServer.join(2000);
    assertTrue(mockSocketServer.finished);
    assertEquals(1, mockSocketServer.loggingEventList.size());

    LoggingEvent remoteEvent = mockSocketServer.loggingEventList.get(0);

    LoggerRemoteView loggerRemoteView = remoteEvent.getLoggerRemoteView();
    assertNotNull(loggerRemoteView);
    assertEquals("root", loggerRemoteView.getName());

    LoggerContextRemoteView loggerContextRemoteView = loggerRemoteView
        .getLoggerContextView();
    assertNotNull(loggerContextRemoteView);
    assertEquals("test", loggerContextRemoteView.getName());
    Map<String, String> props = loggerContextRemoteView.getPropertyMap();
    assertEquals("testValue", props.get("testKey"));
  }

  public void testMessageWithMDC() throws InterruptedException {
    startServer(1);
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);

    MDC.put("key", "testValue");
    logger.debug("test msg");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    mockSocketServer.join(2000);
    assertTrue(mockSocketServer.finished);
    assertEquals(1, mockSocketServer.loggingEventList.size());

    LoggingEvent remoteEvent = mockSocketServer.loggingEventList.get(0);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get("key"));
  }

  public void testMessageWithUpdatedMDC() throws InterruptedException {
    startServer(2);
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);

    MDC.put("key", "testValue");
    logger.debug("test msg");

    MDC.put("key", "updatedTestValue");
    logger.debug("test msg 2");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    mockSocketServer.join(2000);
    assertTrue(mockSocketServer.finished);
    assertEquals(2, mockSocketServer.loggingEventList.size());

    // We observe the second logging event. It should provide us with
    // the updated MDC property.
    LoggingEvent remoteEvent = mockSocketServer.loggingEventList.get(1);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
  }

  private void startServer(int expectedEventNumber) throws InterruptedException {
    mockSocketServer = new MockSocketServer(expectedEventNumber);
    mockSocketServer.start();
    // give MockSocketServer head start
    Thread.sleep(100);
  }

  private void configureClient() {
    lc = new LoggerContext();
    lc.setName("test");
    lc.setProperty("testKey", "testValue");
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setContext(lc);
    socketAppender.setName("socket");
    socketAppender.setPort(4560);
    socketAppender.setRemoteHost("localhost");
    root.addAppender(socketAppender);
    socketAppender.start();
  }
}
