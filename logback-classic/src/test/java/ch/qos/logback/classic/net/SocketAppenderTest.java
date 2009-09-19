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
package ch.qos.logback.classic.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class SocketAppenderTest {

  static final String LIST_APPENDER_NAME = "la";
  static final int JOIN_OR_WAIT_TIMEOUT = 200;
  static final int SLEEP_AFTER_LOG = 100;

  int port = 4561;
  LoggerContext lc = new LoggerContext();
  LoggerContext serverLC = new LoggerContext();
  ListAppender<ILoggingEvent> la = new ListAppender<ILoggingEvent>();
  SocketAppender socketAppender = new SocketAppender();
  private boolean includeCallerData = false;
  private SimpleSocketServer simpleSocketServer;

  @Test
  public void startFailNoRemoteHost() {
    SocketAppender appender = new SocketAppender();
    appender.setContext(lc);
    appender.setPort(123);
    appender.start();
    assertEquals(1, lc.getStatusManager().getCount());
  }

  @Test
  public void recieveMessage() throws InterruptedException {
    fireServer();
    waitForServerToStart();
    configureClient();

    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");

    Thread.sleep(SLEEP_AFTER_LOG);

    simpleSocketServer.close();
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    ILoggingEvent remoteEvent = la.list.get(0);
    assertNull(remoteEvent.getCallerData());
    assertEquals("test msg", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  @Test
  public void recieveWithContext() throws InterruptedException {
    fireServer();
    waitForServerToStart();
    configureClient();

    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");
    Thread.sleep(SLEEP_AFTER_LOG);

    simpleSocketServer.close();
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    ILoggingEvent remoteEvent = la.list.get(0);

    String loggerName = remoteEvent.getLoggerName();
    assertNotNull(loggerName);
    assertEquals(Logger.ROOT_LOGGER_NAME, loggerName);

    LoggerContextVO loggerContextRemoteView = remoteEvent
        .getLoggerContextVO();
    assertNull(remoteEvent.getCallerData());
    assertNotNull(loggerContextRemoteView);
    assertEquals("test", loggerContextRemoteView.getName());
    Map<String, String> props = loggerContextRemoteView.getPropertyMap();
    assertEquals("testValue", props.get("testKey"));
  }

  @Test
  public void messageWithMDC() throws InterruptedException {
    fireServer();
    waitForServerToStart();
    configureClient();

    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);

    MDC.put("key", "testValue");
    logger.debug("test msg");

    Thread.sleep(SLEEP_AFTER_LOG);
    simpleSocketServer.close();
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    assertTrue(simpleSocketServer.isClosed());
    ListAppender<ILoggingEvent> la = getListAppender();
    assertEquals(1, la.list.size());

    ILoggingEvent remoteEvent = la.list.get(0);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get("key"));
    assertNull(remoteEvent.getCallerData());
  }

  // test http://jira.qos.ch/browse/LBCLASSIC-145
  @Test
  public void withCallerData() throws InterruptedException {
    includeCallerData = true;
    fireServer();
    waitForServerToStart();
    configureClient();

    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");

    Thread.sleep(SLEEP_AFTER_LOG);
    simpleSocketServer.close();
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    assertTrue(simpleSocketServer.isClosed());
    ListAppender<ILoggingEvent> la = getListAppender();
    assertEquals(1, la.list.size());

    ILoggingEvent remoteEvent = la.list.get(0);
    assertNotNull(remoteEvent.getCallerData());
  }
  
  @Test
  public void messageWithMarker() throws InterruptedException {
    fireServer();
    waitForServerToStart();

    // Thread.sleep(SLEEP_AFTER_SERVER_START);
    configureClient();

    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);

    Marker marker = MarkerFactory.getMarker("testMarker");
    logger.debug(marker, "test msg");
    Thread.sleep(SLEEP_AFTER_LOG);

    simpleSocketServer.close();
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    ILoggingEvent remoteEvent = la.list.get(0);
    assertEquals("testMarker", remoteEvent.getMarker().getName());
  }

  @Test
  public void messageWithUpdatedMDC() throws InterruptedException {
    fireServer();
    waitForServerToStart();

    configureClient();

    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);

    MDC.put("key", "testValue");
    logger.debug("test msg");

    MDC.put("key", "updatedTestValue");
    logger.debug("test msg 2");
    Thread.sleep(SLEEP_AFTER_LOG);

    simpleSocketServer.close();
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    assertTrue(simpleSocketServer.isClosed());
    ListAppender<ILoggingEvent> la = getListAppender();

    assertEquals(2, la.list.size());

    // We observe the second logging event. It should provide us with
    // the updated MDC property.
    ILoggingEvent remoteEvent = la.list.get(1);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
  }

  @Test
  public void lateServerLaunch() throws InterruptedException {
    socketAppender.setReconnectionDelay(20);
    configureClient();
    Logger logger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");

    fireServer();
    waitForServerToStart();
    Thread.sleep(SLEEP_AFTER_LOG); // allow time for client and server to
                                    // connect
    logger.debug("test msg 2");
    Thread.sleep(SLEEP_AFTER_LOG);

    simpleSocketServer.close();
    Thread.sleep(SLEEP_AFTER_LOG);
    simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    StatusPrinter.print(lc);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    ILoggingEvent remoteEvent = la.list.get(0);
    assertEquals("test msg 2", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  private void waitForServerToStart() throws InterruptedException {
    synchronized (simpleSocketServer) {
      simpleSocketServer.wait(JOIN_OR_WAIT_TIMEOUT);
    }
  }

  private void fireServer() throws InterruptedException {
    Logger root = serverLC.getLogger("root");
    la.setName(LIST_APPENDER_NAME);
    la.setContext(serverLC);
    la.start();
    root.addAppender(la);
    simpleSocketServer = new SimpleSocketServer(serverLC, port);
    simpleSocketServer.start();
    Thread.yield();
  }

  ListAppender<ILoggingEvent> getListAppender() {
    Logger root = serverLC.getLogger("root");
    return (ListAppender<ILoggingEvent>) root.getAppender(LIST_APPENDER_NAME);
  }

  private void configureClient() {
    lc = new LoggerContext();
    lc.setName("test");
    lc.putProperty("testKey", "testValue");
    Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    socketAppender.setContext(lc);
    socketAppender.setName("socket");
    socketAppender.setPort(port);
    socketAppender.setRemoteHost("localhost");
    socketAppender.setIncludeCallerData(includeCallerData);
    root.addAppender(socketAppender);
    socketAppender.start();
  }
}
