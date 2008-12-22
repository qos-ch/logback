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
import ch.qos.logback.core.joran.spi.DefaultClass;
import ch.qos.logback.core.sift.AppenderTracker;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.sift.SiftingAppenderBase;

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

  @Override
  public void start() {
    super.start();
  }

  AppenderTracker<AccessEvent> getAppenderTracker() {
    return appenderTracker;
  }

  @Override
  protected long getTimestamp(AccessEvent event) {
    return event.getTimeStamp();
  }

  @Override
  @DefaultClass(AccessEventDiscriminator.class)
  public void setDiscriminator(Discriminator<AccessEvent> discriminator) {
    super.setDiscriminator(discriminator);
  }
}
