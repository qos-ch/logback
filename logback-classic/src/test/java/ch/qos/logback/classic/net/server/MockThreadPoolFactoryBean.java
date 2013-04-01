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

import java.util.concurrent.Executor;

/**
 * A mock {@link ThreadPoolFactoryBean} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
class MockThreadPoolFactoryBean extends ThreadPoolFactoryBean 
    implements Executor {

  private Runnable lastCommand;
  
  @Override
  public Executor createExecutor() {
    return this;
  }

  public void execute(Runnable command) {
    lastCommand = command;    
  }

  public Runnable getLastCommand() {
    return lastCommand;
  }
  
}
