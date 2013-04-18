/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.TrivialStatusListener;
import sun.security.jca.ProviderList;

public class ContextInitializerTest {

  LoggerContext loggerContext = new LoggerContext();
  Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
    System.clearProperty(ContextInitializer.STATUS_LISTENER_CLASS);
  }


  @Test
  @Ignore  
  // this test works only if logback-test.xml or logback.xml files are on the classpath. 
  // However, this is something we try to avoid in order to simplify the life
  // of users trying to follow the manual and logback-examples from an IDE
  public void reset() throws JoranException {
    {
      new ContextInitializer(loggerContext).autoConfig();
      Appender appender = root.getAppender("STDOUT");
      assertNotNull(appender);
      assertTrue(appender instanceof ConsoleAppender);
    }
    {
      loggerContext.stop();
      Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
      assertNull(appender);
    }
  }

  @Test
  public void autoConfigFromSystemProperties() throws JoranException  {
    doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
    doAutoConfigFromSystemProperties("autoConfigAsResource.xml");
    // test passing a URL. note the relative path syntax with file:src/test/...
    doAutoConfigFromSystemProperties("file:"+ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml"); 
  }
  
  public void doAutoConfigFromSystemProperties(String val) throws JoranException {
    //lc.reset();
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, val);
    new ContextInitializer(loggerContext).autoConfig();
    Appender<ILoggingEvent> appender = root.getAppender("AUTO_BY_SYSTEM_PROPERTY");
    assertNotNull(appender);
  }
  
  @Test
  public void autoStatusListener() throws JoranException {
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS, TrivialStatusListener.class.getName());
    List<StatusListener> statusListenerList = loggerContext.getStatusManager().getCopyOfStatusListenerList();
    assertEquals(0, statusListenerList.size());
    doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
    statusListenerList = loggerContext.getStatusManager().getCopyOfStatusListenerList();
    assertTrue(statusListenerList.size() +" should be 1", statusListenerList.size() == 1);
    // LOGBACK-767
    TrivialStatusListener tsl = (TrivialStatusListener) statusListenerList.get(0);
    assertTrue("expecting at least one event in list", tsl.list.size() > 0);
  }
  
  @Test
  public void autoOnConsoleStatusListener() throws JoranException {
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS,  ContextInitializer.SYSOUT);
    List<StatusListener> sll = loggerContext.getStatusManager().getCopyOfStatusListenerList();
    assertEquals(0, sll.size());
    doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
    sll = loggerContext.getStatusManager().getCopyOfStatusListenerList();
    assertTrue(sll.size() +" should be 1", sll.size() == 1);
  }
}
