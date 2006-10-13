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

import junit.framework.TestCase;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.SyslogConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class SyslogAppenderTest extends TestCase {

  public SyslogAppenderTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testBasic() throws InterruptedException {
      int port = MockSyslogServer.PORT+1;

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
    sa.start();
    assertTrue(sa.isStarted());
    
    String loggerName = this.getClass().getName();
    Logger logger = lc.getLogger(loggerName);
    logger.addAppender(sa);
    String logMsg = "hello";
    logger.debug(logMsg);
    StatusPrinter.print(lc.getStatusManager());
    
    // wait max 2 seconds for mock server to finish. However, it should
    // much sooner than that.
    mockServer.join(8000);
    assertTrue(mockServer.finished);
    assertEquals(1, mockServer.msgList.size());
    String msg = mockServer.msgList.get(0);
   
    String expected = "<"+(SyslogConstants.LOG_MAIL+SyslogConstants.DEBUG_SEVERITY)+">";
    assertTrue(msg.startsWith(expected));

    String first = "<\\d{2}>\\w{3} \\d{2} \\d{2}(:\\d{2}){2} \\w* ";
    String threadName = Thread.currentThread().getName();
   
    assertTrue(msg.matches(first +"\\["+threadName+"\\] "+ loggerName +" " +logMsg));
   
 }
  
  public void testExceptoin() throws InterruptedException {
      int port = MockSyslogServer.PORT+2;
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
    sa.start();
    assertTrue(sa.isStarted());
    
    String loggerName = this.getClass().getName();
    Logger logger = lc.getLogger(loggerName);
    logger.addAppender(sa);
    String logMsg = "hello";
    logger.debug(logMsg, new Exception("just testing"));
    StatusPrinter.print(lc.getStatusManager());
    
    // wait max 2 seconds for mock server to finish. However, it should
    // much sooner than that.
    mockServer.join(8000);
    assertTrue(mockServer.finished);
    assertEquals(1, mockServer.msgList.size());
    String msg = mockServer.msgList.get(0);
   
    String expected = "<"+(SyslogConstants.LOG_MAIL+SyslogConstants.DEBUG_SEVERITY)+">";
    assertTrue(msg.startsWith(expected));

    //String first = "<\\d{2}>\\w{3} \\d{2} \\d{2}(:\\d{2}){2} \\w* ";
    //String threadName = Thread.currentThread().getName();
    System.out.println(msg);
    //assertTrue(msg.matches(first +"\\["+threadName+"\\] "+ loggerName +" " +logMsg));
   
    //fail("check exceptions");
  }
}
