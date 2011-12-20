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
package ch.qos.logback.classic.util;


import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.TrivialStatusListener;
import ch.qos.logback.core.util.Loader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class ContextInitializerTest {

  org.slf4j.Logger logger = LoggerFactory.getLogger(ContextInitializerTest.class);
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
    lc.reset(); // we are going to need this context
  }

  @Test
  @Ignore
  // this test works only if logback-test.xml or logback.xml files are on the classpath. 
  // However, this is something we try to avoid in order to simplify the life
  // of users trying to follows the manual and logback-examples from an IDE
  public void atoconfig() {
    Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
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
      lc.stop();
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
    new ContextInitializer(lc).autoConfig();
    Appender<ILoggingEvent> appender = root.getAppender("AUTO_BY_SYSTEM_PROPERTY");
    assertNotNull(appender);
  }
  
  @Test
  public void autoStatusListener() throws JoranException {
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS, TrivialStatusListener.class.getName());
    List<StatusListener> sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertEquals(0, sll.size());
    doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
    sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertTrue(sll.size() +" should be 1", sll.size() == 1);
  }
  
  @Test
  public void autoOnConsoleStatusListener() throws JoranException {
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS,  ContextInitializer.SYSOUT);
    List<StatusListener> sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertEquals(0, sll.size());
    doAutoConfigFromSystemProperties(ClassicTestConstants.INPUT_PREFIX + "autoConfig.xml");
    sll = lc.getStatusManager().getCopyOfStatusListenerList();
    assertTrue(sll.size() +" should be 1", sll.size() == 1);
  }

  @Test
  public void shouldConfigureFromXmlFile() throws MalformedURLException, JoranException {
    LoggerContext loggerContext = new LoggerContext();
    ContextInitializer initializer = new ContextInitializer(loggerContext);
    assertNull(loggerContext.getObject(CoreConstants.SAFE_JORAN_CONFIGURATION));

    URL configurationFileUrl = Loader.getResource("BOO_logback-test.xml", Thread.currentThread().getContextClassLoader());
    initializer.configureByResource(configurationFileUrl);

    assertNotNull(loggerContext.getObject(CoreConstants.SAFE_JORAN_CONFIGURATION));
  }

  @Test
  public void shouldConfigureFromGroovyScript() throws MalformedURLException, JoranException {
    LoggerContext loggerContext = new LoggerContext();
    ContextInitializer initializer = new ContextInitializer(loggerContext);
    assertNull(loggerContext.getObject(CoreConstants.CONFIGURATION_WATCH_LIST));

    URL configurationFileUrl = Loader.getResource("test.groovy", Thread.currentThread().getContextClassLoader());
    initializer.configureByResource(configurationFileUrl);

    assertNotNull(loggerContext.getObject(CoreConstants.CONFIGURATION_WATCH_LIST));
  }

  @Test
  public void shouldThrowExceptionIfUnexpectedConfigurationFileExtension() throws JoranException {
    LoggerContext loggerContext = new LoggerContext();
    ContextInitializer initializer = new ContextInitializer(loggerContext);

    URL configurationFileUrl = Loader.getResource("README.txt", Thread.currentThread().getContextClassLoader());
    try {
      initializer.configureByResource(configurationFileUrl);
      fail("Should throw LogbackException");
    } catch (LogbackException expectedException) {
      // pass
    }
  }
}
