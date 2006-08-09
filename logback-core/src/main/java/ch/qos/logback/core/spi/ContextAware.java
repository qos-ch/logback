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
import ch.qos.logback.core.status.Status;


public interface ContextAware {

  public void setContext(Context context);
  public Context getContext();
  
  public void addStatus(Status status);
  public void addInfo(String msg);
  
  public void addInfo(String msg, Throwable ex);
  
  public void addWarn(String msg);
  
  public void addWarn(String msg, Throwable ex);
  
  public void addError(String msg);
  
  public void addError(String msg, Throwable ex);  

}
