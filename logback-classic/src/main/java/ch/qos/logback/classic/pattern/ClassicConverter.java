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
package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextAware;
import ch.qos.logback.classic.spi.LoggerContextAwareBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.status.Status;


/**
 * This class serves the super-class of all converters in logback. It extends
 * {@link DynamicConverter} and also implements {@link LoggerContextAware}.
 * 
 * @author Ceki Gulcu
 */
abstract public class ClassicConverter extends DynamicConverter<ILoggingEvent> implements
    LoggerContextAware {

  LoggerContextAwareBase lcab = new LoggerContextAwareBase();

  public void setLoggerContext(LoggerContext lc) {
    lcab.setLoggerContext(lc);
  }

  public void setContext(Context context) {
    lcab.setContext(context);
  }

  public Context getContext() {
    return lcab.getContext();
  }
  
  public void addStatus(Status status) {
    lcab.addStatus(status);
  }

  public void addInfo(String msg) {
    lcab.addInfo(msg);
  }

  public void addInfo(String msg, Throwable ex) {
    lcab.addInfo(msg, ex);
  }

  public void addWarn(String msg) {
    lcab.addWarn(msg);
  }

  public void addWarn(String msg, Throwable ex) {
    lcab.addWarn(msg, ex);
  }

  public void addError(String msg) {
    lcab.addError(msg);
  }

  public void addError(String msg, Throwable ex) {
    lcab.addError(msg, ex);
  }

}
