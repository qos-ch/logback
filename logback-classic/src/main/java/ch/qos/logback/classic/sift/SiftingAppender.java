/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.sift;

import ch.qos.logback.classic.spi.LoggingEvent;
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
public class SiftingAppender extends SiftingAppenderBase<LoggingEvent> {


  String mdcKey;
  String defaultValue;


  @Override
  public void start() {
    int errors = 0;
    if (OptionHelper.isEmpty(mdcKey)) {
      errors++;
      addError("The \"mdcKey\" property must be set");
    }
    if (OptionHelper.isEmpty(defaultValue)) {
      errors++;
      addError("The \"defaultValue\" property must be set");
    }
    setDiscriminator(new MDCBasedDiscriminator(mdcKey, defaultValue));
    if (errors == 0) {
      super.start();
    }
  }

  AppenderTracker<LoggingEvent> getAppenderTracker() {
    return appenderTracker;
  }


  @Override
  protected long getTimestamp(LoggingEvent event) {
    return event.getTimeStamp();
  }

  
  public String getMdcKey() {
    return mdcKey;
  }

  public void setMdcKey(String mdcKey) {
    this.mdcKey = mdcKey;
  }

  /**
   * @see #setDefaultValue(String)
   * @return
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * The default MDC value in case the MDC is not set for
   * {@link #setMdcKey(String) mdcKey}.
   * 
   * <p> For example, if {@link #setMdcKey(String) mdcKey} is set to the value
   * "someKey", and the MDC is not set for "someKey", then this appender will
   * use the default value, which you can set with the help of method.
   * 
   * <p>The "defaultValue" property is set to the value "DEFAULT" by default.
   * 
   * @param defaultValue
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }



}
