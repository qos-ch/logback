/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextAware;
import ch.qos.logback.classic.spi.LoggerContextAwareBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.status.Status;


/**
 * This class serves the super-class of all converters in LOGBack. It extends
 * {@link DynamicConverter} and also implements {@link LoggerContextAware}.
 * 
 * @author Ceki Gulcu
 */
abstract public class ClassicConverter extends DynamicConverter implements
    LoggerContextAware {

  LoggerContextAwareBase lcab = new LoggerContextAwareBase();

  public void setLoggerContext(LoggerContext lc) {
    lcab.setLoggerContext(lc);
  }

  public Logger getLogger() {
    return lcab.getLogger(this);
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
