/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under either
 * the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * 
 * or (per the licensee's choosing)
 * 
 * under the terms of the GNU Lesser General Public License version 2.1 as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.joran.conditional;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;


public class ConditionalTest {

  LoggerContext context = new LoggerContext();
  Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

  
  int diff = RandomUtil.getPositiveInt();
  String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";
  
  @Before
  public void setUp() throws UnknownHostException {
    context.setName("c"+diff);
    context.putProperty("randomOutputDir", randomOutputDir);
    InetAddress localhost = InetAddress.getLocalHost();
    context.putProperty("aHost", localhost.getCanonicalHostName());
  }
 
  
  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(file);
  }
  
  @Test
  public void conditionalConsoleApp() throws JoranException, IOException,
      InterruptedException {

    String configFileAsStr = ClassicTestConstants.JORAN_INPUT_PREFIX
        + "conditional/conditionalConsoleApp.xml";
    configure(configFileAsStr);
    FileAppender fileAppender = (FileAppender) root.getAppender("FILE");
    assertNotNull(fileAppender);
    
    ConsoleAppender consoleAppender = (ConsoleAppender) root.getAppender("CON");
    assertNotNull(consoleAppender);
    StatusPrinter.printIfErrorsOccured(context);


  }
}
