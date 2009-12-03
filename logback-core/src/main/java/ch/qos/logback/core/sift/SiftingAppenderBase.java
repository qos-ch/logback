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
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * This appender serves as the base class for actual SiftingAppenders
 * implemented by the logback-classic and logback-access modules. In a nutshell,
 * a SiftingAppender contains other appenders which it can build dynamically
 * depending on discriminating values supplied by event currently being
 * processed. The built appender is specified as part of a configuration file.
 * 
 * @author Ceki Gulcu
 */
public abstract class SiftingAppenderBase<E> extends
    UnsynchronizedAppenderBase<E> {

  protected AppenderTracker<E> appenderTracker = new AppenderTrackerImpl<E>();
  AppenderFactoryBase<E> appenderFactory;

  Discriminator<E> discriminator;

  public void setAppenderFactory(AppenderFactoryBase<E> appenderFactory) {
    this.appenderFactory = appenderFactory;
  }

  @Override
  public void start() {
    int errors = 0;
    if (discriminator == null) {
      addError("Missing discriminator. Aborting");
      errors++;
    }
    if (!discriminator.isStarted()) {
      addError("Discriminator has not started successfully. Aborting");
      errors++;
    }
    if (errors == 0) {
      super.start();
    }
  }

  @Override
  public void stop() {
    for (Appender<E> appender : appenderTracker.valueList()) {
      appender.stop();
    }
  }

  abstract protected long getTimestamp(E event);

  @Override
  protected void append(E event) {
    if (!isStarted()) {
      return;
    }

    String discriminatingValue = discriminator.getDiscriminatingValue(event);
    long timestamp = getTimestamp(event);

    Appender<E> appender = appenderTracker.get(discriminatingValue, timestamp);

    if (appender == null) {
      try {
        appender = appenderFactory.buildAppender(context, discriminatingValue);
        if (appender != null) {
          appenderTracker.put(discriminatingValue, appender, timestamp);
        }
      } catch (JoranException e) {
        addError("Failed to build appender for [" + discriminatingValue + "]", e);
        return;
      }
    }
    appenderTracker.stopStaleAppenders(timestamp);
    appender.doAppend(event);
  }

  public Discriminator<E> getDiscriminator() {
    return discriminator;
  }

  public void setDiscriminator(Discriminator<E> discriminator) {
    this.discriminator = discriminator;
  }

  
  public String getDiscriminatorKey() {
    if(discriminator != null) {
      return discriminator.getKey();
    } else {
      return null;
    }
  }
}
