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
import ch.qos.logback.classic.StringListAppender;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.DebugUsersTurboFilter;
import ch.qos.logback.classic.turbo.NOPTurboFilter;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.classic.util.Constants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
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

    //StatusPrinter.print(loggerContext.getStatusManager());
  
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

    //StatusPrinter.print(loggerContext.getStatusManager());
  
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

    StatusPrinter.print(loggerContext);
    
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    String msg = "hello world";
    logger.debug("toto");
    logger.debug(msg);
    
    StringListAppender slAppender = (StringListAppender) loggerContext.getLogger("root").getAppender("STR_LIST");
    assertNotNull(slAppender);
    assertEquals(2, slAppender.strList.size());
    assertTrue(slAppender.strList.get(0).contains(" DEBUG - toto"));
  
    String str1 = slAppender.strList.get(1);
    assertTrue(str1.contains("Caller+0"));
    assertTrue(str1.contains(" DEBUG - hello world"));
  }
  
  public void testTurboFilter() throws JoranException {
    //Although this test uses turbo filters, it only checks
    //that Joran can see the xml element and create
    //and place the relevant object correctly.
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/turbo.xml");

    //StatusPrinter.print(loggerContext.getStatusManager());
    
    TurboFilter filter = loggerContext.getFirstTurboFilter();
    assertTrue(filter instanceof NOPTurboFilter);
  }
  
  public void testTurboFilterWithStringList() throws JoranException {
    //Although this test uses turbo filters, it only checks
    //that Joran can see <user> elements, and behave correctly
    //that is call the addUser method and pass the correct values
    //to that method.
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/turbo2.xml");

    //StatusPrinter.print(loggerContext.getStatusManager());
    
    TurboFilter filter = loggerContext.getFirstTurboFilter();
    assertTrue(filter instanceof DebugUsersTurboFilter);
    DebugUsersTurboFilter dutf = (DebugUsersTurboFilter)filter;
    assertEquals(2, dutf.getUsers().size());
  }
  
  
  // COMMENTED_OUT_
  public static Test COMMENTED_OUT_suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BasicJoranTest("testLevel"));
    
    //suite.addTest(new BasicJoranTest("testSimpleList"));

    return suite;
  } 
}
