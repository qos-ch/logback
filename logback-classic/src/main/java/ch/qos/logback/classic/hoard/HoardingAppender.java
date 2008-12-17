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

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.joran.spi.JoranException;
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
public class HoardingAppender extends UnsynchronizedAppenderBase<LoggingEvent> {

  AppenderTracker<LoggingEvent> appenderTracker = new AppenderTrackerImpl<LoggingEvent>();
  //Map<String, Appender<LoggingEvent>> appenderMap = new Hashtable<String, Appender<LoggingEvent>>();

  String mdcKey;
  String defaultValue;

  AppenderFactory appenderFactory;

  void setAppenderFactory(AppenderFactory appenderFactory) {
    this.appenderFactory = appenderFactory;
  }

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
    if (errors == 0) {
      super.start();
    }
  }

  @Override
  public void stop() {
    for (Appender<LoggingEvent> appender : appenderTracker.valueList()) {
      appender.stop();
    }
  }

  @Override
  protected void append(LoggingEvent loggingEvent) {
    if (!isStarted()) {
      return;
    }

    String mdcValue = MDC.get(mdcKey);

    if (mdcValue == null) {
      mdcValue = defaultValue;
    }

    long timestamp = loggingEvent.getTimeStamp();
    
    Appender<LoggingEvent> appender = appenderTracker.get(mdcValue, timestamp);

    if (appender == null) {
      try {
        appender = appenderFactory.buildAppender(context, mdcKey, mdcValue);
        if (appender != null) {
          appenderTracker.put(mdcValue, appender, timestamp);
        }
      } catch (JoranException e) {
        addError("Failed to build appender for " + mdcKey + "=" + mdcValue, e);
        return;
      }
    }
    appenderTracker.stopStaleAppenders(timestamp);
    appender.doAppend(loggingEvent);
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
