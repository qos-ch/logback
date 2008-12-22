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

public class MDCBasedDiscriminator implements Discriminator<LoggingEvent> {

  final String mdcKey;
  final String defaultValue;

  boolean started = true;

  MDCBasedDiscriminator(String mdcKey, String defaultValue) {
    this.mdcKey = mdcKey;
    this.defaultValue = defaultValue;
  }

  public String getDiscriminatingValue(LoggingEvent event) {
    String mdcValue = MDC.get(mdcKey);
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
    started = true;
  }

  public void stop() {
    started = false;
  }

}
