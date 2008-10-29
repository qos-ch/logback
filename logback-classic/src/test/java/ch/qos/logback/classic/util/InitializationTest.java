/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.util;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.TrivialStatusListener;

public class InitializationTest {

  org.slf4j.Logger logger = LoggerFactory.getLogger(InitializationTest.class);
  LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
  Logger root = (Logger) LoggerFactory.getLogger("root");

  @Before
  public void setUp() throws Exception {
    logger.debug("Hello-didily-odily");
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
    System.clearProperty(ContextInitializer.STATUS_LISTENER_CLASS);
    lc.shutdownAndReset();
  }

  @Test
  @Ignore
  // this test works only if logback-test.xml or logback.xml files are on the classpath. 
  // However, this is something we try to avoid in order to simplify the life
  // of users trying to follows the manual and logback-examples from an IDE
  public void atoconfig() {
    Appender appender = root.getAppender("STDOUT");
    assertNotNull(appender);
    assertTrue(appender instanceof ConsoleAppender);
  }

  @Test
  @Ignore  
  // this test works only if logback-test.xml or logback.xml files are on the classpath. 
  // However, this is something we try to avoid in order to simplify the life
  // of users trying to follows the manual and logback-examples from an IDE
  public void reset() throws JoranException {
    {
      new ContextInitializer(lc).autoConfig();
      Appender appender = root.getAppender("STDOUT");
      assertNotNull(appender);
      assertTrue(appender instanceof ConsoleAppender);
    }
    {
      lc.shutdownAndReset();
      Appender appender = root.getAppender("STDOUT");
      assertNull(appender);
    }
  }

  @Test
  public void autoConfigFromSystemProperties() throws JoranException  {
    doAutoConfigFromSystemProperties(TeztConstants.TEST_DIR_PREFIX + "input/autoConfig.xml");
    doAutoConfigFromSystemProperties("autoConfigAsResource.xml");
    // test passing a URL. note the relative path syntax with file:src/test/...
    doAutoConfigFromSystemProperties("file:"+TeztConstants.TEST_DIR_PREFIX + "input/autoConfig.xml"); 
  }
  
  public void doAutoConfigFromSystemProperties(String val) throws JoranException {
    //lc.shutdownAndReset();
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, val);
    new ContextInitializer(lc).autoConfig();
    Appender appender = root.getAppender("AUTO_BY_SYSTEM_PROPERTY");
    assertNotNull(appender);
  }
  
  @Test
  public void autoStatusListener() throws JoranException {
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS, TrivialStatusListener.class.getName());
    List<StatusListener> sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertEquals(0, sll.size());
    doAutoConfigFromSystemProperties(TeztConstants.TEST_DIR_PREFIX + "input/autoConfig.xml");
    sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertTrue(sll.size() +" should be 1", sll.size() == 1);
  }
  
  @Test
  public void autoOnConsoleStatusListener() throws JoranException {
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS,  ContextInitializer.SYSOUT);
    List<StatusListener> sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertEquals(0, sll.size());
    doAutoConfigFromSystemProperties(TeztConstants.TEST_DIR_PREFIX + "input/autoConfig.xml");
    sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertTrue(sll.size() +" should be 1", sll.size() == 1);
  }
}
