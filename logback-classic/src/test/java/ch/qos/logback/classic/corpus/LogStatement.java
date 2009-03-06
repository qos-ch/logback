/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.corpus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;

/**
 * Captures the data contained within a log statement, that is the data that the
 * developer puts in the source code when he writes:
 * 
 * <p>logger.debug("hello world");
 * 
 * @author Ceki G&uuml;lc&uuml; 
 */
public class LogStatement {

  final String loggerName;
  final MessageArgumentTuple mat;
  final Level level;
  final IThrowableProxy throwableProxy;

  public LogStatement(String loggerName, Level level, MessageArgumentTuple mat,
      IThrowableProxy tp) {
    this.loggerName = loggerName;
    this.level = level;
    this.mat = mat;
    this.throwableProxy = tp;
  }

}
