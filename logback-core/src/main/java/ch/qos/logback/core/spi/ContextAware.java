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
