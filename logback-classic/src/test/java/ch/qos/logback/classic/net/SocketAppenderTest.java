/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.*;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.core.read.ListAppender;

import static org.junit.Assert.*;

public class SocketAppenderTest {

  static final String LIST_APPENDER_NAME = "list";
  static final int RECONNECT_DELAY = 1;
  static final int SERVER_LATCH_WAIT_TIMEOUT = 1000;
  static final int APPENDER_LATCH_WAIT_TIMEOUT = 10;

  static int diff = RandomUtil.getPositiveInt();

  static int PORT = 1024 + (diff % 30000);
  static LoggerContext SERVER_LOGGER_CONTEXT = new LoggerContext();
  static ListAppenderWithLatch<ILoggingEvent> LIST_APPENDER = new ListAppenderWithLatch<ILoggingEvent>();
  static private SimpleSocketServer SIMPLE_SOCKET_SERVER;

  String mdcKey = "key" + diff;
  LoggerContext loggerContext = new LoggerContext();
  SocketAppender socketAppender = new SocketAppender();
  private boolean includeCallerData = false;

  @BeforeClass
  public static void beforeClass() throws InterruptedException {
    fireServer();
    waitForServerToStart();
  }

  @AfterClass
  public static void afterClass() {
    closeServer();
  }

  private static void closeServer() {SIMPLE_SOCKET_SERVER.close();}

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
    LIST_APPENDER.list.clear();
  }

  @Test
  public void startFailNoRemoteHost() {
    SocketAppender appender = new SocketAppender();
    appender.setContext(loggerContext);
    appender.setPort(PORT);
    appender.start();
    assertEquals(1, loggerContext.getStatusManager().getCount());
  }

  @Test
  public void receiveMessage() throws InterruptedException {
    updateListAppenderLatch(1);
    configureClient();

    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");

    waitForListAppenderLatch();

    //simpleSocketServer.close();
    //simpleSocketServer.join(JOIN_OR_WAIT_TIMEOUT);
    //assertTrue(simpleSocketServer.isClosed());
    assertEquals(1, LIST_APPENDER.list.size());

    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(0);
    assertNull(remoteEvent.getCallerData());
    assertEquals("test msg", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  @Test
  public void receiveWithContext() throws InterruptedException {
    updateListAppenderLatch(1);
    configureClient();

    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");
    waitForListAppenderLatch();

    assertEquals(1, LIST_APPENDER.list.size());

    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(0);

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
    updateListAppenderLatch(1);
    configureClient();

    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    MDC.put(mdcKey, "testValue");
    root.debug("test msg");

    waitForListAppenderLatch();
    assertEquals(1, LIST_APPENDER.list.size());

    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(0);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get(mdcKey));
    assertNull(remoteEvent.getCallerData());
  }

  // test http://jira.qos.ch/browse/LBCLASSIC-145
  @Test
  public void withCallerData() throws InterruptedException {
    updateListAppenderLatch(1);
    includeCallerData = true;
//    fireServer();
//    waitForServerToStart();
    configureClient();

    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");

    waitForListAppenderLatch();
    assertEquals(1, LIST_APPENDER.list.size());

    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(0);
    assertNotNull(remoteEvent.getCallerData());
  }

  @Test
  public void messageWithMarker() throws InterruptedException {
    updateListAppenderLatch(1);
    configureClient();

    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    Marker marker = MarkerFactory.getMarker("testMarker");
    logger.debug(marker, "test msg");
    waitForListAppenderLatch();

    assertEquals(1, LIST_APPENDER.list.size());

    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(0);
    assertEquals("testMarker", remoteEvent.getMarker().getName());
  }

  @Test
  public void messageWithUpdatedMDC() throws InterruptedException {
    updateListAppenderLatch(2);
    configureClient();

    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    MDC.put(mdcKey, "testValue");
    root.debug("test msg");

    MDC.put(mdcKey, "updatedTestValue");
    root.debug("test msg 2");

    waitForListAppenderLatch();
    assertEquals(2, LIST_APPENDER.list.size());

    // We observe the second logging event. It should provide us with
    // the updated MDC property.
    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(1);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("updatedTestValue", MDCPropertyMap.get(mdcKey));
  }

  @Test
  public void lateServerLaunch() throws InterruptedException {
    closeServer();

    socketAppender.setReconnectionDelay(RECONNECT_DELAY);
    configureClient();
    OnConsoleStatusListener.addNewInstanceToContext(loggerContext);

    Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.debug("test msg");

    fireServer();
    waitForServerToStart();
    updateListAppenderLatch(1);


    int len = 1000/APPENDER_LATCH_WAIT_TIMEOUT;
    for(int i = 0; i < len; i++) {
      logger.debug("test msg lateServerLaunch");
      if(waitForListAppenderLatch()) {
        System.out.println("Success after "+i+" attempts");
        break;
      }
    }

    assertTrue("expecting non-empty list", LIST_APPENDER.list.size() > 0);

    ILoggingEvent remoteEvent = LIST_APPENDER.list.get(0);
    assertEquals("test msg lateServerLaunch", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  private static void waitForServerToStart() throws InterruptedException {
    CountDownLatch latch = SIMPLE_SOCKET_SERVER.getLatch();
    boolean success = latch.await(SERVER_LATCH_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    if (!success) {
      fail("Failed latch wait for server to start");
    }
  }

  private static void fireServer() throws InterruptedException {
    SERVER_LOGGER_CONTEXT.reset();

    Logger root = SERVER_LOGGER_CONTEXT.getLogger("root");

    // we don't want to ignore messages generated by SocketNode
    Logger socketNodeLogger = SERVER_LOGGER_CONTEXT.getLogger(SocketNode.class);
    socketNodeLogger.setLevel(Level.WARN);

    LIST_APPENDER.setName(LIST_APPENDER_NAME);
    LIST_APPENDER.setContext(SERVER_LOGGER_CONTEXT);
    LIST_APPENDER.start();

    root.addAppender(LIST_APPENDER);
    SIMPLE_SOCKET_SERVER = new SimpleSocketServer(SERVER_LOGGER_CONTEXT, PORT);
    CountDownLatch latch = new CountDownLatch(1);
    SIMPLE_SOCKET_SERVER.setLatch(latch);
    SIMPLE_SOCKET_SERVER.start();
  }

  private void updateListAppenderLatch(int count) {
    LIST_APPENDER.setLatch(new CountDownLatch(count));
  }

  private boolean waitForListAppenderLatch() throws InterruptedException {
    CountDownLatch latch = LIST_APPENDER.getLatch();
    boolean success = latch.await(APPENDER_LATCH_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    return success;
  }

  private void configureClient() {
    loggerContext = new LoggerContext();
    loggerContext.setName("test");
    loggerContext.putProperty("testKey", "testValue");
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    socketAppender.setContext(loggerContext);
    socketAppender.setName("socket");
    socketAppender.setPort(PORT);
    socketAppender.setRemoteHost("localhost");
    socketAppender.setIncludeCallerData(includeCallerData);
    root.addAppender(socketAppender);
    socketAppender.start();
  }

  public static class ListAppenderWithLatch<E> extends ListAppender<E> {

    CountDownLatch latch;

    public void setLatch(CountDownLatch latch) {
      this.latch = latch;
    }

    CountDownLatch getLatch() {
      return latch;
    }

    protected void append(E event) {
      super.append(event);
      try {
        latch.countDown();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
