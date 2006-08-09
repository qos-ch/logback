/*
 * Copyright (c) 2004-2005 SLF4J.ORG
 * Copyright (c) 2004-2005 QOS.ch
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, and/or sell copies of  the Software, and to permit persons
 * to whom  the Software is furnished  to do so, provided  that the above
 * copyright notice(s) and this permission notice appear in all copies of
 * the  Software and  that both  the above  copyright notice(s)  and this
 * permission notice appear in supporting documentation.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR  A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * OF  THIRD PARTY  RIGHTS. IN  NO EVENT  SHALL THE  COPYRIGHT  HOLDER OR
 * HOLDERS  INCLUDED IN  THIS  NOTICE BE  LIABLE  FOR ANY  CLAIM, OR  ANY
 * SPECIAL INDIRECT  OR CONSEQUENTIAL DAMAGES, OR  ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS  OF USE, DATA OR PROFITS, WHETHER  IN AN ACTION OF
 * CONTRACT, NEGLIGENCE  OR OTHER TORTIOUS  ACTION, ARISING OUT OF  OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * Except as  contained in  this notice, the  name of a  copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 *
 */

package org.slf4j.impl;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;

/**
 * A wrapper over {@link java.util.logging.Logger java.util.logging.Logger} in
 * conformity with the {@link Logger} interface. Note that the logging levels
 * mentioned in this class refer to those defined in the java.util.logging
 * package.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Peter Royal
 */
public final class JDK14LoggerAdapter extends MarkerIgnoringBase {
  final java.util.logging.Logger logger;

  // WARN: JDK14LoggerAdapter constructor should have only package access so
  // that only JDK14LoggerFactory be able to create one.
  JDK14LoggerAdapter(java.util.logging.Logger logger) {
    this.logger = logger;
  }

  public String getName() {
    return logger.getName();
  }

  /**
   * Is this logger instance enabled for the FINE level?
   * 
   * @return True if this Logger is enabled for level FINE, false otherwise.
   */
  public boolean isDebugEnabled() {
    return logger.isLoggable(Level.FINE);
  }

  //

  /**
   * Log a message object at level FINE.
   * 
   * @param msg -
   *          the message object to be logged
   */
  public void debug(String msg) {
    log(Level.FINE, msg, null);
  }

  /**
   * Log a message at level FINE according to the specified format and argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for level FINE.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void debug(String format, Object arg) {
    if (logger.isLoggable(Level.FINE)) {
      String msgStr = MessageFormatter.format(format, arg);
      log(Level.FINE, msgStr, null);
    }
  }

  /**
   * Log a message at level FINE according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the FINE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void debug(String format, Object arg1, Object arg2) {
    if (logger.isLoggable(Level.FINE)) {
      String msgStr = MessageFormatter.format(format, arg1, arg2);
      log(Level.FINE, msgStr, null);
    }
  }

  /**
   * Log a message at level FINE according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the FINE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void debug(String format, Object[] argArray) {
    if (logger.isLoggable(Level.FINE)) {
      String msgStr = MessageFormatter.arrayFormat(format, argArray);
      log(Level.FINE, msgStr, null);
    }
  }

  /**
   * Log an exception (throwable) at level FINE with an accompanying message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void debug(String msg, Throwable t) {
    log(Level.FINE, msg, t);
  }

  /**
   * Is this logger instance enabled for the INFO level?
   * 
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  public boolean isInfoEnabled() {
    return logger.isLoggable(Level.INFO);
  }

  /**
   * Log a message object at the INFO level.
   * 
   * @param msg -
   *          the message object to be logged
   */
  public void info(String msg) {
    log(Level.INFO, msg, null);
  }

  /**
   * Log a message at level INFO according to the specified format and argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void info(String format, Object arg) {
    if (logger.isLoggable(Level.INFO)) {
      String msgStr = MessageFormatter.format(format, arg);
      log(Level.INFO, msgStr, null);
    }
  }

  /**
   * Log a message at the INFO level according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void info(String format, Object arg1, Object arg2) {
    if (logger.isLoggable(Level.INFO)) {
      String msgStr = MessageFormatter.format(format, arg1, arg2);
      log(Level.INFO, msgStr, null);
    }
  }

  /**
   * Log a message at level INFO according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void info(String format, Object[] argArray) {
    if (logger.isLoggable(Level.INFO)) {
      String msgStr = MessageFormatter.arrayFormat(format, argArray);
      log(Level.INFO, msgStr, null);
    }
  }

  /**
   * Log an exception (throwable) at the INFO level with an accompanying
   * message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void info(String msg, Throwable t) {
    log(Level.INFO, msg, t);
  }

  /**
   * Is this logger instance enabled for the WARNING level?
   * 
   * @return True if this Logger is enabled for the WARNING level, false
   *         otherwise.
   */
  public boolean isWarnEnabled() {
    return logger.isLoggable(Level.WARNING);
  }

  /**
   * Log a message object at the WARNING level.
   * 
   * @param msg -
   *          the message object to be logged
   */
  public void warn(String msg) {
    log(Level.WARNING, msg, null);
  }

  /**
   * Log a message at the WARNING level according to the specified format and
   * argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the WARNING level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void warn(String format, Object arg) {
    if (logger.isLoggable(Level.WARNING)) {
      String msgStr = MessageFormatter.format(format, arg);
      log(Level.WARNING, msgStr, null);
    }
  }

  /**
   * Log a message at the WARNING level according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the WARNING level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void warn(String format, Object arg1, Object arg2) {
    if (logger.isLoggable(Level.WARNING)) {
      String msgStr = MessageFormatter.format(format, arg1, arg2);
      log(Level.WARNING, msgStr, null);
    }
  }

  /**
   * Log a message at level WARNING according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the WARNING level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void warn(String format, Object[] argArray) {
    if (logger.isLoggable(Level.WARNING)) {
      String msgStr = MessageFormatter.arrayFormat(format, argArray);
      log(Level.WARNING, msgStr, null);
    }
  }

  /**
   * Log an exception (throwable) at the WARNING level with an accompanying
   * message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void warn(String msg, Throwable t) {
    log(Level.WARNING, msg, t);
  }

  /**
   * Is this logger instance enabled for level SEVERE?
   * 
   * @return True if this Logger is enabled for level SEVERE, false otherwise.
   */
  public boolean isErrorEnabled() {
    return logger.isLoggable(Level.SEVERE);
  }

  /**
   * Log a message object at the SEVERE level.
   * 
   * @param msg -
   *          the message object to be logged
   */
  public void error(String msg) {
    log(Level.SEVERE, msg, null);
  }

  /**
   * Log a message at the SEVERE level according to the specified format and
   * argument.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the SEVERE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg
   *          the argument
   */
  public void error(String format, Object arg) {
    if (logger.isLoggable(Level.SEVERE)) {
      String msgStr = MessageFormatter.format(format, arg);
      log(Level.SEVERE, msgStr, null);
    }
  }

  /**
   * Log a message at the SEVERE level according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the SEVERE level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param arg1
   *          the first argument
   * @param arg2
   *          the second argument
   */
  public void error(String format, Object arg1, Object arg2) {
    if (logger.isLoggable(Level.SEVERE)) {
      String msgStr = MessageFormatter.format(format, arg1, arg2);
      log(Level.SEVERE, msgStr, null);
    }
  }

  /**
   * Log a message at level INFO according to the specified format and
   * arguments.
   * 
   * <p>
   * This form avoids superfluous object creation when the logger is disabled
   * for the INFO level.
   * </p>
   * 
   * @param format
   *          the format string
   * @param argArray
   *          an array of arguments
   */
  public void error(String format, Object[] argArray) {
    if (logger.isLoggable(Level.SEVERE)) {
      String msgStr = MessageFormatter.arrayFormat(format, argArray);
      log(Level.SEVERE, msgStr, null);
    }
  }

  /**
   * Log an exception (throwable) at the SEVERE level with an accompanying
   * message.
   * 
   * @param msg
   *          the message accompanying the exception
   * @param t
   *          the exception (throwable) to log
   */
  public void error(String msg, Throwable t) {
    log(Level.SEVERE, msg, t);
  }

  
  /**
   * Log the message at the specified level with the specified
   * throwable if any. This method creates a LogRecord and fills
   * in caller date before calling this instance's JDK14 logger. 
   * 
   * See bug report #13 for more details.
   * @param level
   * @param msg
   * @param t
   */
  private void log(Level level, String msg, Throwable t) {
    // millis and thread are filled by the constructor
    LogRecord record = new LogRecord(level, msg);
    record.setLoggerName(getName());
    record.setThrown(t);
    fillCallerData(record);
    logger.log(record);

  }

  static String SELF = JDK14LoggerAdapter.class.getName();
  static String SUPER = MarkerIgnoringBase.class.getName();

  /**
   * Fill in caller data if possible. 
   * 
   * @param record The record to update
   */
  final private void fillCallerData(LogRecord record) {
    StackTraceElement[] steArray = new Throwable().getStackTrace();

    int selfIndex = -1;
    for (int i = 0; i < steArray.length; i++) {
      final String className = steArray[i].getClassName();
      if (className.equals(SELF) || className.equals(SUPER)) {
        selfIndex = i;
        break;
      }
    }

    int found = -1;
    for (int i = selfIndex + 1; i < steArray.length; i++) {
      final String className = steArray[i].getClassName();
      if (!(className.equals(SELF) || className.equals(SUPER))) {
        found = i;
        break;
      }
    }

    if (found != -1) {
      StackTraceElement ste = steArray[found];
      // setting the class name has the side effect of setting
      // the needToInferCaller variable to false.
      record.setSourceClassName(ste.getClassName());
      record.setSourceMethodName(ste.getMethodName());
    }
  }
}
