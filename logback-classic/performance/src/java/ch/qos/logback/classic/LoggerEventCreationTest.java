/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import junit.framework.TestCase;


// it takes about 250 nanos to create a LoggingEvent
public class LoggerEventCreationTest extends TestCase {

  public LoggerEventCreationTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testEventLoop() {
    long len = 10000000;
    System.out.println((eventLoop(len))/len);
  }
  
  public void testFfilterMethod() {
    long len = 10000000;
    System.out.println((methodCalLoop(len))/len);
  }
  
  long eventLoop(long len) {
    Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    LoggingEvent le = null;
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      le = new  LoggingEvent("", logger, Level.DEBUG, "asdasdasd", null, null);
    }
    le.getLevel();
    long end = System.nanoTime();
    return end-start;
  }
  
  long methodCalLoop(long len) {
    Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
    long start = System.nanoTime();
    for(long i = 0; i < len; i++) {
      filterMethod(logger, Level.DEBUG, "asdasdasd", null, null);
    }
    long end = System.nanoTime();
    return end-start;
  }
  
  int filterMethod(Logger logger, Level level, String msg, Object[] params, Exception e) {
    if(level.levelInt < 100) {
      return 1;
    } else {
      return 0;
    }
  }
}
