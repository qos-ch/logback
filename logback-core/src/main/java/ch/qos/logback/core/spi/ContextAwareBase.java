/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.core.spi;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;


/**
 * A helper class that implements ContextAware methods. This class can be either
 * extended or alternatively included as a component.
 * 
 * @author Ceki G&uumllc&uuml;
 */
public class ContextAwareBase implements ContextAware {

  private int noContextWarning = 0;
  protected Context context;

  public void setContext(Context context) {
    if (this.context == null) {
      this.context = context;
    } else if (this.context != context) {
      throw new IllegalStateException("Context has been already set");
    }
  }

  public Context getContext() {
    return this.context;
  }

  public StatusManager getStatusManager() {
    if (context == null) {
      return null;
    }
    return context.getStatusManager();
  }

  public void addStatus(Status status) {
    if (context == null) {
      if (noContextWarning++ == 0) {
        System.out.println("LOGBACK: No context given for " + this);
      }
      return;
    }
    StatusManager sm = context.getStatusManager();
    if (sm != null) {
      sm.add(status);
    }
  }

  public void addInfo(String msg) {
    addStatus(new InfoStatus(msg, this));
  }

  public void addInfo(String msg, Throwable ex) {
    addStatus(new InfoStatus(msg, this, ex));
  }

  public void addWarn(String msg) {
    addStatus(new WarnStatus(msg, this));
  }

  public void addWarn(String msg, Throwable ex) {
    addStatus(new WarnStatus(msg, this, ex));
  }

  public void addError(String msg) {
    addStatus(new ErrorStatus(msg, this));
  }

  public void addError(String msg, Throwable ex) {
    addStatus(new ErrorStatus(msg, this, ex));
  }

}
