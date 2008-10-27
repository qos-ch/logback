/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.status;



public class InfoStatus extends StatusBase {
  public InfoStatus(String msg, Object origin) {
    super(Status.INFO, msg, origin);
  }

  public InfoStatus(String msg, Object origin, Throwable t) {
    super(Status.INFO, msg, origin, t);
  }

 }
