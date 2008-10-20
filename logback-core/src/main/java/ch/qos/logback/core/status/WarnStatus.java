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



public class WarnStatus extends StatusBase {

 
  public WarnStatus(String msg, Object origin) {
    super(Status.WARN, msg, origin);
  }

  public WarnStatus( String msg, Object origin, Throwable t) {
    super(Status.WARN, msg, origin, t);
  }

 }
