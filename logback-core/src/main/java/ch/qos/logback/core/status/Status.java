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
package ch.qos.logback.core.status;

import java.util.Iterator;


public interface Status  {

  public final int INFO = 0;
  public final int WARN = 1;
  public final int ERROR = 2;
  
  int getLevel();
  int getEffectiveLevel();
  Object getOrigin();
  String getMessage();
  Throwable getThrowable();
  Long getDate();
  
  public boolean hasChildren();
  public void add(Status child);
  public boolean remove(Status child);
  public Iterator<Status> iterator();

}
