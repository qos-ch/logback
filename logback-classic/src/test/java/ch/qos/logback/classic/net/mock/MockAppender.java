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
package ch.qos.logback.classic.net.mock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A mock {@link AppenderBase} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockAppender extends AppenderBase<ILoggingEvent> {

  private final Lock lock = new ReentrantLock();
  private final Condition appendCondition = lock.newCondition();
  
  private int eventCount;
  private ILoggingEvent lastEvent;
  
  @Override
  protected void append(ILoggingEvent eventObject) {
    lock.lock();
    try {
      eventCount++;
      lastEvent = eventObject;
      appendCondition.signalAll();      
    }
    finally {
      lock.unlock();
    }
  }
  
  public boolean awaitAppend(long delay) throws InterruptedException{
    lock.lock();
    try {
      int count = eventCount;
      boolean timeout = false;
      while (count == eventCount && !timeout) {
        timeout = !appendCondition.await(delay, TimeUnit.MILLISECONDS);
      }
      return !timeout;
    }
    finally {
      lock.unlock();
    }
  }

  public int getEventCount() {
    return eventCount;
  }

  public ILoggingEvent getLastEvent() {
    return lastEvent;
  }

}
