/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>
 * This class is a minimal implementation of the origianl
 * org.apache.log4j.Logger class delegating all calls to a
 * {@link org.slf4j.Logger} instance, which in turn will delegate to a final
 * logging system chosen by the user..
 * </p>
 * 
 * <p>
 * Log4j's <code>debug()</code>, <code>info()</code>, <code>warn()</code>,
 * <code>error()</code> printing methods are directly mapped to their SLF4J
 * equivalents. Log4j's <code>trace()</code> printing method is mapped to
 * SLF4J's <code>debug()</code> method with a TRACE marker. Log4j's
 * <code>fatal()</code> printing method is mapped to SLF4J's
 * <code>error()</code> method with a FATAL marker.
 * 
 * @author S&eacute;bastien Pennec
 * @author Ceki G&uuml;lc&uuml;
 */

public class Category {

  private String name;

  private ch.qos.logback.classic.Logger lbLogger;

  private static Marker TRACE_MARKER = MarkerFactory.getMarker("TRACE");
  private static Marker FATAL_MARKER = MarkerFactory.getMarker("FATAL");

  Category(String name) {
    this.name = name;
    lbLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
  }

  public static Logger getLogger(String name) {
    return Log4jLoggerFactory.getLogger(name);
  }

  public static Logger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  /**
   * Does the obvious.
   * 
   * @return
   */
  public static Logger getRootLogger() {
    return getLogger("root");
  }

  /**
   * Returns the obvious.
   * 
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Delegates to {@link org.slf4j.Logger#isDebugEnabled} method of the SLF4J
   * API, in addition, the call is marked with a marker named "TRACE".
   */
  public boolean isTraceEnabled() {
    return lbLogger.isDebugEnabled(TRACE_MARKER);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#isDebugEnabled} method of the SLF4J
   * API.
   */
  public boolean isDebugEnabled() {
    return lbLogger.isDebugEnabled();
  }

  /**
   * Delegates to {@link org.slf4j.Logger#isInfoEnabled} method of the SLF4J
   * API.
   */
  public boolean isInfoEnabled() {
    return lbLogger.isInfoEnabled();
  }

  /**
   * Delegates to {@link org.slf4j.Logger#isWarnEnabled} method of the SLF4J
   * API.
   */
  public boolean isWarnEnabled() {
    return lbLogger.isWarnEnabled();
  }

  /**
   * Delegates to {@link org.slf4j.Logger#isErrorEnabled} method of the SLF4J
   * API.
   */
  public boolean isErrorEnabled() {
    return lbLogger.isErrorEnabled();
  }

  /**
   * Delegates to {@link org.slf4j.Logger#debug(String)} method of the SLF4J
   * API, in addition, the call is marked with a marker named "TRACE".
   */
  public void trace(Object message) {
    lbLogger.debug(TRACE_MARKER, (String) message);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#debug(String,Throwable)} method of the
   * SLF4J API, in addition, the call is marked with a marker named "TRACE".
   */
  public void trace(Object message, Throwable t) {
    lbLogger.debug(TRACE_MARKER, (String) message, t);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#debug(String)} method of the SLF4J
   * API.
   */
  public void debug(Object message) {
    // casting to String as SLF4J only accepts String instances, not Object
    // instances.
    lbLogger.debug((String) message);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#debug(String,Throwable)} method of the
   * SLF4J API.
   */
  public void debug(Object message, Throwable t) {
    lbLogger.debug((String) message, t);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#info(String)} method of the SLF4J API.
   */
  public void info(Object message) {
    lbLogger.info((String) message);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#info(String, Throwable)} method of the
   * SLF4J API.
   */
  public void info(Object message, Throwable t) {
    lbLogger.info((String) message, t);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#warn(String)} method of the SLF4J API.
   */
  public void warn(Object message) {
    lbLogger.warn((String) message);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#warn(String,Throwable)} method of the
   * SLF4J API.
   */
  public void warn(Object message, Throwable t) {
    lbLogger.warn((String) message, t);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#error(String)} method of the SLF4J
   * API.
   */
  public void error(Object message) {
    lbLogger.error((String) message);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#error(String,Throwable)} method of the
   * SLF4J API.
   */
  public void error(Object message, Throwable t) {
    lbLogger.error((String) message, t);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#error(String)} method of the SLF4J
   * API, in addition, the call is marked with a marker named "FATAL".
   */
  public void fatal(Object message) {
    lbLogger.error(FATAL_MARKER, (String) message);
  }

  /**
   * Delegates to {@link org.slf4j.Logger#error(String,Throwable)} method of the
   * SLF4J API, in addition, the call is marked with a marker named "FATAL".
   */
  public void fatal(Object message, Throwable t) {
    lbLogger.error(FATAL_MARKER, (String) message, t);
  }

  public void log(String FQCN, Priority p, Object msg, Throwable t) {
    ch.qos.logback.classic.Level level;
    switch (p.level) {
    case Priority.DEBUG_INT:
      level = ch.qos.logback.classic.Level.DEBUG;
      break;
    case Priority.INFO_INT:
      level = ch.qos.logback.classic.Level.INFO;
      break;
    case Priority.WARN_INT:
      level = ch.qos.logback.classic.Level.WARN;
      break;
    case Priority.ERROR_INT:
      level = ch.qos.logback.classic.Level.ERROR;
      break;
    case Priority.FATAL_INT:
      level = ch.qos.logback.classic.Level.ERROR;
      break;
    default:
      throw new IllegalStateException("Unknown Priority " + p);
    }
    lbLogger.filterAndLog(FQCN, null, level, msg.toString(), null, t);
  }

}
