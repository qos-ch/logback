/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.LoggerFactoryBinder;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextJNDISelector;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.selector.DefaultContextSelector;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.OptionHelper;

/**
 * 
 * The binding of {@link LoggerFactory} class with an actual instance of
 * {@link ILoggerFactory} is performed using information returned by this class.
 * 
 * @author <a href="http://www.qos.ch/log4j/">Ceki G&uuml;lc&uuml;</a>
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  private ContextSelector contextSelector;

  /**
   * The unique instance of this class.
   */
  public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
  private static final String loggerFactoryClassStr = ContextSelector.class
      .getName();

  private StaticLoggerBinder() {
    initialize();
  }

  public void initialize() {
    try {
      // let's configure a default context
      LoggerContext defaultLoggerContext = new LoggerContext();
      defaultLoggerContext.setName("default");
      ContextInitializer.autoConfig(defaultLoggerContext);

      // See if a special context selector is needed
      String contextSelectorStr = OptionHelper.getSystemProperty(
          ClassicGlobal.LOGBACK_CONTEXT_SELECTOR, null);
      if (contextSelectorStr == null) {
        contextSelector = new DefaultContextSelector(defaultLoggerContext);
      } else if (contextSelectorStr.equals("JNDI")) {
        // if jndi is specified, let's use the appropriate class
        contextSelector = new ContextJNDISelector(defaultLoggerContext);
      }
    } catch (Exception e) {
      // we should never get here
      Util.reportFailure("Failed to instantiate ["
          + LoggerContext.class.getName() + "]", e);
    }
  }

  public ILoggerFactory getLoggerFactory() {
    if (contextSelector == null) {
      throw new IllegalStateException(
          "contextSelector cannot be null. See also http://logback.qos.ch/codes.html#null_CS");
    }
    return contextSelector.getLoggerContext();
  }

  public String getLoggerFactoryClassStr() {
    return loggerFactoryClassStr;
  }

  /**
   * Return the {@link ContextSelector} instance in use.
   * 
   * @return the ContextSelector instance in use
   */
  public ContextSelector getContextSelector() {
    return contextSelector;
  }

}
