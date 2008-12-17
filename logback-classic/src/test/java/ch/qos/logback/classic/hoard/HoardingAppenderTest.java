/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.hoard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class HoardingAppenderTest {

  static String PREFIX = TeztConstants.TEST_DIR_PREFIX + "input/joran/hoard/";
  		
  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(this.getClass().getName());
  Logger root = loggerContext.getLogger(LoggerContext.ROOT_NAME);

  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(loggerContext);
    jc.doConfigure(file);
  }

  @Test
  public void unsetDefaultValueProperty() throws JoranException {
    configure(PREFIX + "unsetDefaultValueProperty.xml");
    logger.debug("hello");
    HoardingAppender ha = (HoardingAppender) root.getAppender("HOARD");
    assertFalse(ha.isStarted());
    
  }

  @Test
  public void smoke() throws JoranException {
    configure(PREFIX + "smoke.xml");
    logger.debug("smoke");
    long timestamp = 0;
    HoardingAppender ha = (HoardingAppender) root.getAppender("HOARD");
    ListAppender<LoggingEvent> listAppender = (ListAppender<LoggingEvent>) ha.appenderTracker.get("smoke", timestamp);
    StatusPrinter.print(loggerContext);
    
    assertNotNull(listAppender);
    List<LoggingEvent> eventList = listAppender.list;
    assertEquals(1, listAppender.list.size());
    assertEquals("smoke", eventList.get(0).getMessage());
  }

  @Test
  public void testLevel() throws JoranException {
    configure(PREFIX + "hoard0.xml");
    logger.debug("ss");
    //StatusPrinter.print(loggerContext);
    
  }

}
