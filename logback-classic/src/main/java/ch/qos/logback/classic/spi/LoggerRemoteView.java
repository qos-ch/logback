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
package ch.qos.logback.classic.spi;

import java.io.Serializable;

import ch.qos.logback.classic.LoggerContext;

/**
 * An interface that allows Logger objects and LoggerSer objects to be used the
 * same way be client of the LoggingEvent object.
 * <p>
 * See {@link LoggerContextVO} for the rationale of this class.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LoggerRemoteView implements Serializable {

  private static final long serialVersionUID = 5028223666108713696L;

  final LoggerContextVO loggerContextView;
  final String name;

  public LoggerRemoteView(String name, LoggerContext lc) {
    this.name = name;
    assert lc.getLoggerContextRemoteView() != null;
    loggerContextView = lc.getLoggerContextRemoteView();
  }

  public LoggerContextVO getLoggerContextView() {
    return loggerContextView;
  }

  public String getName() {
    return name;
  }
  

}
