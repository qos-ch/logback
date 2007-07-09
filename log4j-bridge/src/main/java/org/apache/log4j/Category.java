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
import org.slf4j.spi.LocationAwareLogger;

import ch.qos.logback.classic.Level;

/**
 * <p>
 * This class is a minimal implementation of the origianl
 * org.apache.log4j.Logger class delegating all calls to a
 * {@link org.slf4j.Logger.Logger} instance.
 * </p>
 * 
 * <p>
 * Log4j's <code>debug()</code>, <code>info()</code>, <code>warn()</code>,
 * <code>error()</code> printing methods are directly mapped to their logback
 * equivalents. Log4j's <code>trace()</code> printing method is mapped to
 * logback's <code>debug()</code> method with a TRACE marker. Log4j's
 * <code>fatal()</code> printing method is mapped to logback's
 * <code>error()</code> method with a FATAL marker.
 * 
 * @author S&eacute;bastien Pennec
 * @author Ceki G&uuml;lc&uuml;
 */

public class Category {

  private String name;

  private org.slf4j.Logger lbLogger;
  private org.slf4j.spi.LocationAwareLogger locationAwareLogger;
  
  private static Marker TRACE_MARKER = MarkerFactory.getMarker("TRACE");
  private static Marker FATAL_MARKER = MarkerFactory.getMarker("FATAL");

  Category(String name) {
    this.name = name;
    lbLogger = LoggerFactory.getLogger(name);
    if(lbLogger instanceof LocationAwareLogger) {
      locationAwareLogger = (LocationAwareLogger) lbLogger;
    }
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
   * Delegates to {@link ch.qos.logback.classic.Logger#isTraceEnabled} 
   * method of logback.
   */
  public boolean isTraceEnabled() {
    return lbLogger.isTraceEnabled();
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#isDebugEnabled} method of logback
   */
  public boolean isDebugEnabled() {
    return lbLogger.isDebugEnabled();
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#isInfoEnabled} method of logback
   */
  public boolean isInfoEnabled() {
    return lbLogger.isInfoEnabled();
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#isWarnEnabled} method of logback
   */
  public boolean isWarnEnabled() {
    return lbLogger.isWarnEnabled();
  }
  
  public boolean isEnabledFor(Priority p) {
    return isEnabledFor(Level.toLevel(p.level));
  }

  public boolean isEnabledFor(Level l) {
    switch (l.levelInt) {
    case Level.TRACE_INT:
      return lbLogger.isTraceEnabled();
    case Level.DEBUG_INT:
      return lbLogger.isDebugEnabled();
    case Level.INFO_INT:
      return lbLogger.isInfoEnabled();
    case Level.WARN_INT:
      return lbLogger.isWarnEnabled();
    case Level.ERROR_INT:
      return lbLogger.isErrorEnabled();
    case Priority.FATAL_INT:
      return lbLogger.isErrorEnabled();
    }
    return false;
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#isErrorEnabled} method of logback
   */
  public boolean isErrorEnabled() {
    return lbLogger.isErrorEnabled();
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String)} method of logback, 
   * in addition, the call is marked with a marker named "TRACE".
   */
  public void trace(Object message) {
    lbLogger.debug(TRACE_MARKER, convertToString(message));
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String,Throwable)} 
   * method of logback in addition, the call is marked with a marker named "TRACE".
   */
  public void trace(Object message, Throwable t) {
    lbLogger.debug(TRACE_MARKER, convertToString(message), t);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String,Object)} 
   * method of logback in addition, the call is marked with a marker named "TRACE".
   */
  public void trace(Object message, Object o) {
    lbLogger.debug(TRACE_MARKER, convertToString(message), o);
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String,Object,Object)} 
   * method of logback in addition, the call is marked with a marker named "TRACE".
   */
  public void trace(String message, Object arg1, Object arg2) {
    lbLogger.debug(TRACE_MARKER, message, arg1, arg2);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String)} method of
   * logback.
   */
  public void debug(Object message) {
    // casting to String as SLF4J only accepts String instances, not Object
    // instances.
    lbLogger.debug(convertToString(message));
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String,Throwable)} 
   * method of logback.
   */
  public void debug(Object message, Throwable t) {
    lbLogger.debug(convertToString(message), t);
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String,Object)} 
   * method of logback.
   */
  public void debug(Object message, Object o) {
    lbLogger.debug(convertToString(message), o);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#debug(String,Object,Object)} 
   * method of logback.
   */
  public void debug(String message, Object arg1, Object arg2) {
    lbLogger.debug(message, arg1, arg2);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#info(String)} 
   * method of logback.
   */
  public void info(Object message) {
    lbLogger.info(convertToString(message));
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#info(String,Throwable)} 
   * method of logback.
   */
  public void info(Object message, Throwable t) {
    lbLogger.info(convertToString(message), t);
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#info(String,Object)} 
   * method of logback.
   */
  public void info(Object message, Object o) {
    lbLogger.info(convertToString(message), o);
  }  
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#info(String,Object,Object)} 
   * method of logback.
   */
  public void info(String message, Object arg1, Object arg2) {
    lbLogger.info(message, arg1, arg2);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#warn(String)} 
   * method of logback.
   */
  public void warn(Object message) {
    lbLogger.warn(convertToString(message));
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#warn(String,Throwable)} 
   * method of logback.
   */
  public void warn(Object message, Throwable t) {
    lbLogger.warn(convertToString(message), t);
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#warn(String,Object)} 
   * method of logback.
   */
  public void warn(Object message, Object o) {
    lbLogger.warn(convertToString(message), o);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#warn(String,Object,Object)} 
   * method of logback.
   */
  public void warn(String message, Object arg1, Object arg2) {
    lbLogger.warn(message, arg1, arg2);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String)} 
   * method of logback.
   */
  public void error(Object message) {
    lbLogger.error(convertToString(message));
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String,Throwable)} 
   * method of logback.
   */
  public void error(Object message, Throwable t) {
    lbLogger.error(convertToString(message), t);
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String,Object)} 
   * method of logback.
   */
  public void error(Object message, Object o) {
    lbLogger.error(convertToString(message), o);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String,Object,Object)} 
   * method of logback.
   */
  public void error(String message, Object arg1, Object arg2) {
    lbLogger.error(message, arg1, arg2);
  }
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String)} 
   * method of logback.
   */
  public void fatal(Object message) {
    lbLogger.error(FATAL_MARKER, convertToString(message));
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String,Throwable)} 
   * method of logback in addition, the call is marked with a marker named "FATAL".
   */
  public void fatal(Object message, Throwable t) {
    lbLogger.error(FATAL_MARKER, convertToString(message), t);
  }

  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String,Object)} 
   * method of logback in addition, the call is marked with a marker named "FATAL".
   */
  public void fatal(Object message, Object o) {
    lbLogger.error(FATAL_MARKER, convertToString(message), o);
  } 
  
  /**
   * Delegates to {@link ch.qos.logback.classic.Logger#error(String,Object,Object)} 
   * method of logback in addition, the call is marked with a marker named "FATAL".
   */
  public void fatal(String message, Object arg1, Object arg2) {
    lbLogger.error(FATAL_MARKER, message, arg1, arg2);
  } 
  
  public void log(String FQCN, Priority p, Object msg, Throwable t) {
    int levelInt = priorityToLevelInt(p);
    if(locationAwareLogger != null) {
      if(msg != null) {
        locationAwareLogger.log(null, FQCN, levelInt, msg.toString(), t); 
      } else {
        locationAwareLogger.log(null, FQCN, levelInt, null, t); 
      }
    } else {
      throw new UnsupportedOperationException("The logger ["+lbLogger+"] does not seem to be location aware.");
    }
   
  }
  
  private int priorityToLevelInt(Priority p) {
    switch (p.level) {
    case Level.TRACE_INT:
      return LocationAwareLogger.TRACE_INT;
    case Priority.DEBUG_INT:
      return LocationAwareLogger.DEBUG_INT;
    case Priority.INFO_INT:
      return LocationAwareLogger.INFO_INT;
    case Priority.WARN_INT:
      return LocationAwareLogger.WARN_INT;
    case Priority.ERROR_INT:
      return LocationAwareLogger.ERROR_INT;
    case Priority.FATAL_INT:
      return LocationAwareLogger.ERROR_INT;
    default:
      throw new IllegalStateException("Unknown Priority " + p);
    }
  }
  
  private final String convertToString(Object message) {
    if (message == null) {
      return (String)message;
    } else {
      return message.toString();
    }
  }

}
