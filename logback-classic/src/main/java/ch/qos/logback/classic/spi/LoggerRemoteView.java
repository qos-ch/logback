/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.spi;

import java.io.Serializable;

import ch.qos.logback.classic.LoggerContext;

/**
 * An interface that allows Logger objects and LoggerSer objects to be used the
 * same way be client of the LoggingEvent object.
 * <p>
 * See {@link LoggerContextRemoteView} for the rationale of this class.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggerRemoteView implements Serializable {

  private static final long serialVersionUID = 5028223666108713696L;

  final LoggerContextRemoteView loggerContextView;
  final String name;

  public LoggerRemoteView(String name, LoggerContext lc) {
    this.name = name;
    assert lc.getLoggerContextRemoteView() != null;
    loggerContextView = lc.getLoggerContextRemoteView();
  }

  public LoggerContextRemoteView getLoggerContextView() {
    return loggerContextView;
  }

  public String getName() {
    return name;
  }

}
