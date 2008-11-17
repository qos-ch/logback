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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.StatusChecker;



abstract public class AbstractAppenderTest<E>  {
  
  
  abstract protected AppenderBase<E> getAppender();
  abstract protected AppenderBase<E> getConfiguredAppender();

  @Test
  public void testNewAppender() {
    // new appenders should be inactive
    AppenderBase appender = getAppender();
    assertFalse( appender.isStarted()); 
  }
  
  @Test
  public void testConfiguredAppender() {
    AppenderBase appender = getConfiguredAppender();
    appender.start();
    assertTrue(appender.isStarted());
   
    appender.stop();
    assertFalse(appender.isStarted());
    
  }
  
  @Test
  public void testNoStart() {
    AppenderBase<E> appender = getAppender();
    Context context = new ContextBase();
    appender.setContext(context);
    appender.setName("doh");
    // is null OK?
    appender.doAppend(null);
    StatusChecker checker = new StatusChecker(context.getStatusManager());
    //StatusPrinter.print(context.getStatusManager());
    assertTrue(checker.containsMatch("Attempted to append to non started appender \\[doh\\]."));
  }
}


