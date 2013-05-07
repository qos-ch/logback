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
    this.executorService = executorService;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executorService;
  }
}
