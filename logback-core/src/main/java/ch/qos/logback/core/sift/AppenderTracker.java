/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

import java.util.*;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.NOPAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.AbstractComponentTracker;
import ch.qos.logback.core.spi.ContextAwareImpl;

/**
 * Track appenders by a key. When an appender is not used for
 * longer than THRESHOLD, stop it.
 *
 * @author Ceki Gulcu
 * @author Tommy Becker
 */
public class AppenderTracker<E> extends AbstractComponentTracker<Appender<E>> {

  int nopaWarningCount = 0;

  final Context context;
  final AppenderFactoryBase<E> appenderFactory;
  final ContextAwareImpl contextAware;

  public AppenderTracker(Context context, AppenderFactoryBase<E> appenderFactory) {
    super();
    this.context = context;
    this.appenderFactory = appenderFactory;
    this.contextAware = new ContextAwareImpl(context);
  }


  @Override
  protected void stop(Appender<E> component) {
    component.stop();
  }

  @Override
  protected Appender<E> buildComponent(String key) {
    Appender<E> appender = null;
    try {
      appender = appenderFactory.buildAppender(context, key);
    } catch (JoranException je) {
      contextAware.addError("Error while building appender with discriminating value [" + key + "]");
    }
    if (appender == null) {
      appender = buildNOPAppender(key);
    }

    return appender;
  }


  private NOPAppender<E> buildNOPAppender(String key) {
    if (nopaWarningCount < CoreConstants.MAX_ERROR_COUNT) {
      nopaWarningCount++;
      contextAware.addError("Building NOPAppender for discriminating value [" + key + "]");
    }
    NOPAppender<E> nopa = new NOPAppender<E>();
    nopa.setContext(context);
    nopa.start();
    return nopa;
  }

  @Override
  protected boolean isComponentStale(Appender<E> appender) {
    return !appender.isStarted();
  }

}
