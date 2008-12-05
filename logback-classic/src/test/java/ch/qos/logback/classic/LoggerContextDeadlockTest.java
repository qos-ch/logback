/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LoggerContextDeadlockTest {

  LoggerContext loggerContext = new LoggerContext();
  JoranConfigurator jc = new JoranConfigurator();
  GetLoggerThread getLoggerThread = new GetLoggerThread(loggerContext);

  @Before
  public void setUp() throws Exception {
    jc.setContext(loggerContext);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(timeout=20000)
  public void testLBCLASSIC_81() throws JoranException {


    getLoggerThread.start();
    for (int i = 0; i < 500; i++) {
      ByteArrayInputStream baos = new ByteArrayInputStream(new String(
      "<configuration><root level=\"DEBUG\"/></configuration>").getBytes());
      jc.doConfigure(baos);
    }
  }

  class GetLoggerThread extends Thread {

    final LoggerContext loggerContext;
    GetLoggerThread(LoggerContext loggerContext) {
      this.loggerContext = loggerContext;
    }
    @Override
    public void run() {
      for (int i = 0; i < 10000; i++) {
        if(i % 100 == 0) {
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
          }
        }
        loggerContext.getLogger("a" + i);
      }
    }
  }

}
