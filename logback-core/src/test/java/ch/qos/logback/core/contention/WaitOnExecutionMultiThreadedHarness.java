/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.contention;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class WaitOnExecutionMultiThreadedHarness extends AbstractMultiThreadedHarness {
  Context context;
  StatusUtil statusUtil;
  int count;

  public WaitOnExecutionMultiThreadedHarness(Context context, int count) {
    this.context = context;
    this.statusUtil = new StatusUtil(context);
    this.count = count;
  }

  @Override
  void waitUntilEndCondition() throws InterruptedException {
    while (visibleResets() < count) {
      Thread.yield();
    }
  }

  private int visibleResets() {
    return statusUtil.matchCount("Detected change in");
  }
}
