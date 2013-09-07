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
package ch.qos.logback.core.net.mock;

import java.util.List;
import java.util.concurrent.ExecutorService;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;

/**
 * A mock {@link Context} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockContext extends ContextBase {

  private final ExecutorService executorService;
  
  private Status lastStatus;
  
  public MockContext() {
    this(new MockExecutorService());
  }
  
  public MockContext(ExecutorService executorService) {
    this.setStatusManager(new MockStatusManager());
    this.executorService = executorService;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executorService;
  }

  public Status getLastStatus() {
    return lastStatus;
  }

  public void setLastStatus(Status lastStatus) {
    this.lastStatus = lastStatus;
  }

  private class MockStatusManager implements StatusManager {

    public void add(Status status) {
      lastStatus = status;
    }

    public List<Status> getCopyOfStatusList() {
      throw new UnsupportedOperationException();
    }

    public int getCount() {
      throw new UnsupportedOperationException();
    }

    public void add(StatusListener listener) {
      throw new UnsupportedOperationException();
    }

    public void remove(StatusListener listener) {
      throw new UnsupportedOperationException();
    }

    public void clear() {
      throw new UnsupportedOperationException();
    }

    public List<StatusListener> getCopyOfStatusListenerList() {
      throw new UnsupportedOperationException();
    }
     
  }
  
}
