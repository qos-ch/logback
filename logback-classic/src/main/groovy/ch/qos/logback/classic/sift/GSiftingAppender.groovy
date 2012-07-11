/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.sift

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.sift.AppenderTrackerImpl
import ch.qos.logback.core.sift.Discriminator
import ch.qos.logback.core.sift.AppenderTracker
import ch.qos.logback.core.Appender

import ch.qos.logback.classic.gaffer.ConfigurationContributor
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.helpers.NOPAppender

/**
 * @author Ceki G&uuml;c&uuml;
 */

// The GMaven plugin does not support generics, so we use   AppenderBase instead of AppenderBase<ILoggingEvent>
class GSiftingAppender extends AppenderBase implements ConfigurationContributor {


  protected AppenderTracker<ILoggingEvent> appenderTracker = new AppenderTrackerImpl<ILoggingEvent>();
  Discriminator<ILoggingEvent> discriminator;
  Closure builderClosure;

  def Map<String, String> getMappings() {
    return [sift: "sift"]
  }

  @Override
  public void start() {
    int errors = 0;
    if (discriminator == null) {
      addError("Missing discriminator. Aborting");
      errors++;
    }
    if (!discriminator?.isStarted()) {
      addError("Discriminator has not started successfully. Aborting");
      errors++;
    }

    if (builderClosure == null) {
      addError("Missing builder closure. Aborting");
      errors++;
    }
    if (errors == 0) {
      super.start();
    }
  }

  @Override
  public void stop() {
    for (Appender<ILoggingEvent> appender: appenderTracker.valueList()) {
      appender.stop();
    }
  }

  protected long getTimestamp(ILoggingEvent event) {
    return event.getTimeStamp();
  }

  Appender buildAppender(String value) {
    String key = getDiscriminatorKey()

    ZSiftingDelegate zd = new ZSiftingDelegate(getDiscriminatorKey(), value)
    zd.context = context
    zd.metaClass."$key" = value

    //Closure newBuilder = builderClosure.clone()
    builderClosure.delegate = zd;
    builderClosure.resolveStrategy = Closure.DELEGATE_FIRST
    Appender a = builderClosure()
    return a
  }

  @Override
  public void append(Object object) {
    ILoggingEvent event = (ILoggingEvent) object;
    if (!isStarted()) {
      return;
    }

    String discriminatingValue = discriminator.getDiscriminatingValue(event);
    long timestamp = getTimestamp(event);

    Appender<ILoggingEvent> appender = appenderTracker.get(discriminatingValue, timestamp);
    if (appender == null) {
      try {
        appender = buildAppender(discriminatingValue);
        if (appender == null) {
          appender = buildNOPAppender(discriminatingValue);
        }
        appenderTracker.put(discriminatingValue, appender, timestamp);

      } catch (Throwable e) {
        addError("Failed to build appender for [" + discriminatingValue + "]",
                e);
        return;
      }
    }
    appenderTracker.stopStaleAppenders(timestamp);

    appender.doAppend(event);
  }

  int nopaWarningCount = 0;

  NOPAppender<ILoggingEvent> buildNOPAppender(String discriminatingValue) {
    if (nopaWarningCount < CoreConstants.MAX_ERROR_COUNT) {
      nopaWarningCount++;
      addError("Failed to build an appender for discriminating value [" + discriminatingValue + "]");
    }
    NOPAppender<ILoggingEvent> nopa = new NOPAppender<ILoggingEvent>();
    nopa.setContext(context);
    nopa.start();
    return nopa;
  }

  void build() {
    int r = builderClosure();
    println "r=$r"

  }

  void sift(Closure clo) {
    builderClosure = clo;
  }


  public AppenderTracker getAppenderTracker() {
    return appenderTracker;
  }

  public String getDiscriminatorKey() {
    if (discriminator != null) {
      return discriminator.getKey();
    } else {
      return null;
    }
  }
}
