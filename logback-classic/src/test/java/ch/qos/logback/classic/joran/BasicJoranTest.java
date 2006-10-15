/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.joran;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.Constants;
import ch.qos.logback.core.appender.ListAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class BasicJoranTest extends TestCase {

  public BasicJoranTest(String name) {
    super(name);
  }
  
  public void testSimpleList() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/simpleList.xml");

    StatusPrinter.print(loggerContext.getStatusManager());
  
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
  
  public void testLevel() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/simpleLevel.xml");

    StatusPrinter.print(loggerContext.getStatusManager());
  
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(LoggerContext.ROOT_NAME);
    ListAppender listAppender = (ListAppender) root.getAppender("LIST");
    assertEquals(0, listAppender.list.size());
    String msg = "hello world";
    logger.debug(msg);
    assertEquals(0, listAppender.list.size());
    //LoggingEvent le = (LoggingEvent) listAppender.list.get(0);
    //assertEquals(msg, le.getMessage());
  }
  
  public void testEval() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/callerData.xml");

    StatusPrinter.print(loggerContext.getStatusManager());
  
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    String msg = "hello world";
    logger.debug("toto");
    logger.debug(msg);
  }
  
  
  // COMMENTED_OUT_
  public static Test COMMENTED_OUT_suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BasicJoranTest("testLevel"));
    
    //suite.addTest(new BasicJoranTest("testSimpleList"));

    return suite;
  }
  
  
  
}
