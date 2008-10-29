/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.testUtil;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.AppenderBase;

public class NotifyingListAppender extends AppenderBase<AccessEvent> {

  public List<AccessEvent> list = new ArrayList<AccessEvent>();
  
  protected void append(AccessEvent e) {
    list.add(e);
    synchronized (this) {
      this.notify();
    }
  }
}
