/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ClassPackagingData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

public class ExtendedThrowableProxyConverter extends ThrowableProxyConverter {

  @Override
  protected void extraData(StringBuilder builder, StackTraceElementProxy step) {

    if (step != null) {
      ClassPackagingData pi = step.getClassPackagingData();
      if (pi != null) {
        builder.append(" [").append(pi.getCodeLocation()).append(':').append(
            pi.getVersion()).append(']');
      }
    }
  }

  protected void prepareLoggingEvent(ILoggingEvent event) {
    
  }

}
