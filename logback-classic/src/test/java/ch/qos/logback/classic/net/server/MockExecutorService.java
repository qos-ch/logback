/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net.server;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An {@link ExecutorService} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockExecutorService extends AbstractExecutorService {
  
  private boolean shutdown;

  public void shutdown() {
    this.shutdown = true;
  }

  public List<Runnable> shutdownNow() {
    shutdown();
    return Collections.emptyList();
  }

  public boolean isShutdown() {
    return shutdown;
  }

  public boolean isTerminated() {
    return true;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    return true;
  }

  public void execute(Runnable command) {
  }
  
}