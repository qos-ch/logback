/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.appender;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.layout.DummyLayout;
import ch.qos.logback.core.layout.NopLayout;


public class FileAppenderTest extends AbstractAppenderTest {

  public FileAppenderTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  protected AppenderBase getAppender() {
    return new FileAppender();
  }

  protected AppenderBase getConfiguredAppender() {
    FileAppender appender = new FileAppender();
    appender.setLayout(new NopLayout());
    appender.setFile("temp.log");
    appender.setName("temp.log");
    appender.setContext(new ContextBase());
    appender.start();
    return appender;
  }
  
  public void test() {
    FileAppender appender = new FileAppender();
    appender.setLayout(new DummyLayout());
    appender.setAppend(false);
    appender.setFile("temp.log");
    appender.setName("temp.log");
    appender.setContext(new ContextBase());
    appender.start();
    appender.doAppend(new Object());
  }

}
