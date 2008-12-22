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

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

public class MDCBasedDiscriminator extends ContextAwareBase implements Discriminator<LoggingEvent> {

  String key;
  String defaultValue;

  boolean started = false;

  public MDCBasedDiscriminator() {
  }

  public String getDiscriminatingValue(LoggingEvent event) {
    String mdcValue = MDC.get(key);
    if (mdcValue == null) {
      return defaultValue;
    } else {
      return mdcValue;
    }
  }

  public boolean isStarted() {
    return started;
  }

  public void start() {
    int errors = 0;
    if (OptionHelper.isEmpty(key)) {
      errors++;
      addError("The \"Key\" property must be set");
    }
    if (OptionHelper.isEmpty(defaultValue)) {
      errors++;
      addError("The \"DefaultValue\" property must be set");
    }
    if (errors == 0) {
      started = true;
    }
  }

  public void stop() {
    started = false;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
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
   * @param defaultValue
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  

}
