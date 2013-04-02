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
package ch.qos.logback.core.net.server;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A mock {@link ThreadPoolFactoryBean} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
class MockThreadPoolFactoryBean extends ThreadPoolFactoryBean {

  private final MockExecutorService executorService = 
      new MockExecutorService();
  
  private Runnable lastCommand;
  
  @Override
  public ExecutorService createExecutor() {
    return executorService;
  }

  public Runnable getLastCommand() {
    return lastCommand;
  }
  
  private class MockExecutorService extends AbstractExecutorService {

    public void shutdown() {
    }

    public List<Runnable> shutdownNow() {
      return Collections.emptyList();
    }

    public boolean isShutdown() {
      return true;
    }

    public boolean isTerminated() {
      return true;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException {
      return true;
    }

    public void execute(Runnable command) {
      lastCommand = command;
    }
    
  }
  
}
