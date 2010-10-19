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

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.StatusPrinter;

import java.util.List;

/**
 * Print all new incoming status messages on the console.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class OnConsoleStatusListener extends ContextAwareBase implements StatusListener, LifeCycle {


  boolean isStarted = false;
  long retrospective = 300;

  private void print(Status status) {
    StringBuilder sb = new StringBuilder();
    StatusPrinter.buildStr(sb, "", status);
    System.out.print(sb);
  }

  public void addStatusEvent(Status status) {
    if (!isStarted)
      return;
    print(status);
  }

  /**
   * Print status messages retrospectively
   */
  private void retrospectivePrint() {
    long now = System.currentTimeMillis();
    StatusManager sm = context.getStatusManager();
    List<Status> statusList = sm.getCopyOfStatusList();
    for (Status status : statusList) {
      long timestamp = status.getDate();
      if (now - timestamp < retrospective) {
        print(status);
      }
    }
  }

  public void start() {
    isStarted = true;
    if (retrospective > 0) {
      retrospectivePrint();
    }
  }

  public void setRetrospective(long retrospective) {
    this.retrospective = retrospective;
  }

  public long getRetrospective() {
    return retrospective;
  }

  public void stop() {
    isStarted = false;
  }

  public boolean isStarted() {
    return isStarted;
  }
}
