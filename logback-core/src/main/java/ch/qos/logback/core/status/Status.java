/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
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
  
  public boolean hasChildren();
  public void add(Status child);
  public boolean remove(Status child);
  public Iterator<Status> iterator();

}
