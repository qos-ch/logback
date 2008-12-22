/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.sift;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.sift.SiftingAppenderBase;
import ch.qos.logback.core.util.OptionHelper;

/**
 * This appender can contains other appenders which it can build dynamically
 * depending on MDC values. The built appender is specified as part of a
 * configuration file.
 * 
 * <p>See the logback manual for further details.
 * 
 * 
 * @author Ceki Gulcu
 */
public class SiftingAppender extends SiftingAppenderBase<AccessEvent> {

  String keyName;

  @Override
  public void start() {
    int errors = 0;
    if (OptionHelper.isEmpty(keyName)) {
      errors++;
      addError("The \"keyName\" property must be set");
    }
    if (errors == 0) {
      super.start();
    }
  }

  AppenderTracker<AccessEvent> getAppenderTracker() {
    return appenderTracker;
  }

  @Override
  protected long getTimestamp(AccessEvent event) {
    return event.getTimeStamp();
  }

  public String getKeyName() {
    return keyName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

}
