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
package org.slf4j.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.LoggerFactoryBinder;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextJNDISelector;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.selector.DefaultContextSelector;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * 
 * The binding of {@link LoggerFactory} class with an actual instance of
 * {@link ILoggerFactory} is performed using information returned by this class.
 * 
 * @author <a href="http://www.qos.ch/log4j/">Ceki G&uuml;lc&uuml;</a>
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  /**
   * Declare the version of the SLF4J API this implementation is compiled
   * against. The value of this field is usually modified with each release.
   */
  // to avoid constant folding by the compiler, this field must *not* be final
  public static String REQUESTED_API_VERSION = "1.5.8"; // !final

  final static String NULL_CS_URL = CoreConstants.CODES_URL + "#null_CS";

  /**
   * The unique instance of this class.
   */
  private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

  static {
    SINGLETON.init();
  }

  private boolean initialized = false;
  private LoggerContext defaultLoggerContext = new LoggerContext();
  private ContextSelector contextSelector;

  private StaticLoggerBinder() {
    defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
  }

  public static StaticLoggerBinder getSingleton() {
    return SINGLETON;
  }

  /**
   * Package access for testing purposes.
   */
  static void reset() {
    SINGLETON = new StaticLoggerBinder();
    SINGLETON.init();
  }

  /**
   * Package access for testing purposes.
   */
  void init() {
    try {
      try {
        new ContextInitializer(defaultLoggerContext).autoConfig();
      } catch (JoranException je) {
        Util.reportFailure("Failed to auto configure default logger context",
            je);
      }
      StatusPrinter.printInCaseOfErrorsOrWarnings(defaultLoggerContext);

      // See if a special context selector is needed
      String contextSelectorStr = OptionHelper
          .getSystemProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR);
      if (contextSelectorStr == null) {
        contextSelector = new DefaultContextSelector(defaultLoggerContext);
      } else if (contextSelectorStr.equals("JNDI")) {
        // if jndi is specified, let's use the appropriate class
        contextSelector = new ContextJNDISelector(defaultLoggerContext);
      } else {
        contextSelector = dynamicalContextSelector(defaultLoggerContext,
            contextSelectorStr);
      }
      initialized = true;
    } catch (Throwable t) {
      // we should never get here
      Util.reportFailure("Failed to instantiate ["
          + LoggerContext.class.getName() + "]", t);
    }
  }

  /**
   * Intantiate the context selector class designated by the user. The selector
   * must have a constructor taking a LoggerContext instance as an argument.
   * 
   * @param defaultLoggerContext
   * @param contextSelectorStr
   * @return an instance of the designated context selector class
   * @throws ClassNotFoundException
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  static ContextSelector dynamicalContextSelector(
      LoggerContext defaultLoggerContext, String contextSelectorStr)
      throws ClassNotFoundException, SecurityException, NoSuchMethodException,
      IllegalArgumentException, InstantiationException, IllegalAccessException,
      InvocationTargetException {
    Class<?> contextSelectorClass = Loader.loadClass(contextSelectorStr);
    Constructor cons = contextSelectorClass
        .getConstructor(new Class[] { LoggerContext.class });
    return (ContextSelector) cons.newInstance(defaultLoggerContext);
  }

  public ILoggerFactory getLoggerFactory() {
    if (!initialized) {
      return defaultLoggerContext;
    }

    if (contextSelector == null) {
      throw new IllegalStateException(
          "contextSelector cannot be null. See also " + NULL_CS_URL);
    }
    return contextSelector.getLoggerContext();
  }

  public String getLoggerFactoryClassStr() {
    return contextSelector.getClass().getName();
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
