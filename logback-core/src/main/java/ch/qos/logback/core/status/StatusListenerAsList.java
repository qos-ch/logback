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

import java.util.ArrayList;
import java.util.List;

/**
 * Collect all incoming events in a list.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class StatusListenerAsList implements StatusListener {

  List<Status> statusList = new ArrayList<Status>();

  public void addStatusEvent(Status status) {
    statusList.add(status);
  }

  public List<Status> getStatusList() {
    return statusList;
  }
  
  
}
