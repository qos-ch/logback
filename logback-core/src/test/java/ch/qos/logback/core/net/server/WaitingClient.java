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
package ch.qos.logback.core.net.server;

import ch.qos.logback.core.net.server.Client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A mock {@link Client} that notifies waiting thread when it has started,
 * and waits to be interrupted before exiting.
 *
 * @author Carl Harris
 */
class WaitingClient implements Client {


  private final Lock lock = new ReentrantLock();
  private final Condition startCondition = lock.newCondition();
  private final Condition stopCondition = lock.newCondition();

  private boolean running;
  private boolean closed;

  public void run() {
    running = true;
    signalStartCondition();
    while (running && !Thread.currentThread().isInterrupted()) {
      try {
        awaitStopCondition(Integer.MAX_VALUE);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public void close() {
    running = false;
    closed = true;
    signalStopCondition();
  }

  void awaitStartCondition(long timeout) throws InterruptedException {
    lock.lock();
    try {
      startCondition.await(timeout, TimeUnit.MILLISECONDS);
    } finally {
      lock.unlock();
    }
  }
  void awaitStopCondition(long timeout) throws InterruptedException {
    lock.lock();
    try {
      stopCondition.await(timeout, TimeUnit.MILLISECONDS);
    } finally {
      lock.unlock();
    }
  }

  private void signalStartCondition() {
    lock.lock();
    try {
      startCondition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  private void signalStopCondition() {
    lock.lock();
    try {
      stopCondition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public synchronized boolean isRunning() {
    return running;
  }

  public synchronized boolean isClosed() {
    return closed;
  }

}
