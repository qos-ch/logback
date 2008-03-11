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


import static org.junit.Assert.*;

import java.util.Map;


import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class SocketAppenderTest  {

  static final String LIST_APPENDER_NAME = "la";
  int port = 4561;
  LoggerContext lc = new LoggerContext();
  LoggerContext serverLC = new LoggerContext();
  ListAppender<LoggingEvent> la = new ListAppender<LoggingEvent>();
  SocketAppender socketAppender = new SocketAppender();
  
  private SimpleSocketServer simpleSocketServer;
 
  @Test
  public void testStartFailNoRemoteHost() {
    SocketAppender appender = new SocketAppender();
    appender.setContext(lc);
    appender.setPort(123);
    appender.start();
    assertEquals(1, lc.getStatusManager().getCount());
  }

  @Test
  public void testRecieveMessage() throws InterruptedException {
    fireServer();
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test msg");
    
    Thread.sleep(100);
    
    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    simpleSocketServer.close();
    simpleSocketServer.join(2000);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    LoggingEvent remoteEvent = la.list.get(0);
    assertEquals("test msg", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  @Test
  public void testRecieveWithContext() throws InterruptedException {
    fireServer();
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test msg");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    Thread.sleep(100);
    simpleSocketServer.close();
    simpleSocketServer.join(2000);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    LoggingEvent remoteEvent = la.list.get(0);

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

  @Test
  public void testMessageWithMDC() throws InterruptedException {
    fireServer();
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);

    MDC.put("key", "testValue");
    logger.debug("test msg");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    Thread.sleep(100);
    simpleSocketServer.close();
    simpleSocketServer.join(2000);
    assertTrue(simpleSocketServer.isClosed());
    ListAppender<LoggingEvent> la = getListAppender();
    assertEquals(1, la.list.size());

    LoggingEvent remoteEvent = la.list.get(0);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get("key"));
  }
  
  @Test
  public void testMessageWithMarker() throws InterruptedException {
    fireServer();
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    
    Marker marker = MarkerFactory.getMarker("testMarker");
    logger.debug(marker, "test msg");

    Thread.sleep(100);
    
    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    simpleSocketServer.close();
    simpleSocketServer.join(2000);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    LoggingEvent remoteEvent = la.list.get(0);
    assertEquals("testMarker", remoteEvent.getMarker().getName());
  }

  @Test
  public void testMessageWithUpdatedMDC() throws InterruptedException {
    fireServer();
    configureClient();

    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);

    MDC.put("key", "testValue");
    logger.debug("test msg");

    MDC.put("key", "updatedTestValue");
    logger.debug("test msg 2");

    Thread.sleep(100);
    
    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.
    simpleSocketServer.close();
    simpleSocketServer.join(2000);
    assertTrue(simpleSocketServer.isClosed());
    ListAppender<LoggingEvent> la = getListAppender();
    
    assertEquals(2, la.list.size());

    // We observe the second logging event. It should provide us with
    // the updated MDC property.
    LoggingEvent remoteEvent = la.list.get(1);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
  }

  @Test
  public void lateServerLaunch() throws InterruptedException {
    socketAppender.setReconnectionDelay(20);
    configureClient();
    Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.debug("test msg");

    fireServer();
    synchronized (simpleSocketServer) {
      simpleSocketServer.wait(1000);  
    }
    // give the server a little more time
    Thread.yield();
    logger.debug("test msg 2");
    
    simpleSocketServer.close();
    simpleSocketServer.join(2000);
    StatusPrinter.print(lc);
    assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, la.list.size());

    LoggingEvent remoteEvent = la.list.get(0);
    assertEquals("test msg 2", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }
  
  private void fireServer() throws InterruptedException {
    Logger root = serverLC.getLogger("root");
    la.setName(LIST_APPENDER_NAME);
    la.setContext(serverLC);
    la.start();
    root.addAppender(la);
    simpleSocketServer = new SimpleSocketServer(serverLC, port);
    simpleSocketServer.start();
  }

  
  ListAppender<LoggingEvent> getListAppender() {
    Logger root = serverLC.getLogger("root");
    return (ListAppender<LoggingEvent>) root.getAppender(LIST_APPENDER_NAME);
  }
  
  private void configureClient() {
    lc = new LoggerContext();
    lc.setName("test");
    lc.putProperty("testKey", "testValue");
    Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
    socketAppender.setContext(lc);
    socketAppender.setName("socket");
    socketAppender.setPort(port);
    socketAppender.setRemoteHost("localhost");
    root.addAppender(socketAppender);
    socketAppender.start();
  }
}
