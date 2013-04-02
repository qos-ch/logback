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

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;

/**
 * A {@link ServerRunner} that sends logging events to remote logger
 * clients.
 *
 * @author Carl Harris
 */
class RemoteLoggerServerRunner 
    extends ConcurrentServerRunner<RemoteLoggerClient> {

  private final int clientQueueSize;
  
  /**
   * Constructs a new server runner.
   * @param listener the listener from which the server will accept new
   *    clients
   * @param executor that will be used to execute asynchronous tasks 
   *    on behalf of the runner.
   * @param queueSize size of the event queue that will be maintained for
   *    each client
   */
  public RemoteLoggerServerRunner(
      ServerListener<RemoteLoggerClient> listener, Executor executor,
      int clientQueueSize) {
    super(listener, executor);
    this.clientQueueSize = clientQueueSize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean configureClient(RemoteLoggerClient client) {
    client.setContext(getContext());
    client.setQueue(new ArrayBlockingQueue<Serializable>(clientQueueSize));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void logInfo(String message) {
    addInfo(message);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void logError(String message) {
    addError(message);
  }

}
