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

import junit.framework.TestCase;

import org.slf4j.MDC;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class XConfiguratorTestX extends TestCase {

  public XConfiguratorTestX(String name) {
    super(name);
  }
  
  public void testSimpleList() throws JoranException { 
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext(); 
    jc.setContext(loggerContext);
    jc.doConfigure(TeztConstants.TEST_DIR_PREFIX + "input/joran/turboDynamicThreshold.xml");

    StatusPrinter.print(loggerContext.getStatusManager());
  
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(LoggerContext.ROOT_NAME);
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
  
 
//  public static Test suite() {
//    TestSuite suite = new TestSuite();
//    suite.addTestSuite(XConfiguratorTest.class);
//    //suite.addTest(new JoranConfiguratorTest("testEvaluatorFilter"));
//    return suite;
//  } 
}
