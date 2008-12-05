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

import ch.qos.logback.core.util.StatusPrinter;

/**
 * Print all new incoming status messages on the console.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class OnConsoleStatusListener implements StatusListener {


  public void addStatusEvent(Status status) {
    StringBuilder sb = new StringBuilder();
    StatusPrinter.buildStr(sb, "", status);
    System.out.print(sb);
  }
}
