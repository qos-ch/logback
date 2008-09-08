/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableDataPoint;


public class ExtendedThrowableProxyConverter extends ThrowableProxyConverter {

  @Override
  protected void extraData(StringBuilder builder, ThrowableDataPoint tdp) {
    StackTraceElementProxy step = tdp.getStackTraceElementProxy();
    if(step != null) {
      ClassPackagingData pi = step.getClassPackagingData();
      if(pi != null) {
        builder.append(" [").append(pi.getCodeLocation()).append(':').append(pi.getVersion()).append(']');
      }
    }
  }
}
